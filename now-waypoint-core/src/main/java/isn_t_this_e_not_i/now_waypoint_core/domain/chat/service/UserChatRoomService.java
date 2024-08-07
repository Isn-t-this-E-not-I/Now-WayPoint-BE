package isn_t_this_e_not_i.now_waypoint_core.domain.chat.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.MessageType;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.ChatMessageResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.ErrorMessageResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response.*;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity.ChatMessage;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity.ChatRoom;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity.UserChatRoom;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.repository.ChatRoomRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.repository.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserChatRoomService {
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String CHAT_ROOM_MESSAGES_PREFIX = "chatroom:messages:";

    // 채팅방 생성 -> /queue/update/{userNickName} 으로 채팅방 업데이트 웹소켓 메시지 전송
    @Transactional
    public void createChatRoom(String logInUserId, String[] nicknames) {
        User logInUser = userRepository.findByLoginId(logInUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 로그인 ID가 없습니다: " + logInUserId));
        String logInUserNickname = logInUser.getNickname();

        // 로그인 유저의 닉네임이 nicknames 배열에 포함되어 있는지 체크
        boolean hasDuplicateNickname = Arrays.stream(nicknames).anyMatch(nickname -> nickname.equals(logInUserNickname));
        if (hasDuplicateNickname && nicknames.length == 1) {
            ErrorMessageResponse response = ErrorMessageResponse.builder()
                    .messageType(MessageType.ERROR)
                    .content("나와의 채팅방 생성에 실패했습니다.")
                    .build();
            messagingTemplate.convertAndSend("/queue/chatroom/" + logInUserNickname, response);
            throw new IllegalArgumentException("나와의 채팅방 생성에 실패했습니다.");
        } else if(hasDuplicateNickname){
            ErrorMessageResponse response = ErrorMessageResponse.builder()
                    .messageType(MessageType.ERROR)
                    .content("사용자의 닉네임을 입력하셨습니다.")
                    .build();
            messagingTemplate.convertAndSend("/queue/chatroom/" + logInUserNickname, response);
            throw new IllegalArgumentException("사용자의 닉네임을 입력하셨습니다.");
        }

        Optional<ChatRoom> existingChatRoom = findChatRoomWithUsers(logInUserId, nicknames);
        if (existingChatRoom.isPresent()) {
            ErrorMessageResponse response = ErrorMessageResponse.builder()
                    .messageType(MessageType.ERROR_DUPLICATE)
                    .chatRoomId(existingChatRoom.get().getId())
                    .content("해당하는 채팅방이 이미 존재합니다.")
                    .build();
            messagingTemplate.convertAndSend("/queue/chatroom/" + logInUserNickname, response);
            throw new IllegalArgumentException("해당하는 채팅방이 이미 존재합니다.");
        }
        String chatRoomName = logInUserNickname + ", " + Arrays.stream(nicknames).collect(Collectors.joining(","));
        ChatRoom chatRoom = ChatRoom.builder().name(chatRoomName).build();
        chatRoomRepository.save(chatRoom);

        List<ChatRoomUserResponse> allUsers = new ArrayList<>();
        List<UserChatRoom> userChatRooms = new ArrayList<>();

        for (String nickname : nicknames) {
            User user = userRepository.findByNickname(nickname)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 닉네임이 없습니다: " + nickname));

            allUsers.add(ChatRoomUserResponse.builder()
                    .userNickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build());

            userChatRooms.add(UserChatRoom.builder()
                    .user(user)
                    .chatRoom(chatRoom)
                    .build());
        }

        allUsers.add(ChatRoomUserResponse.builder()
                .userNickname(logInUserNickname)
                .profileImageUrl(logInUser.getProfileImageUrl())
                .build());

        userChatRooms.add(UserChatRoom.builder()
                .user(logInUser)
                .chatRoom(chatRoom)
                .build());

        CreateChatRoomResponse response = CreateChatRoomResponse.builder()
                .messageType(MessageType.CREATE)
                .chatRoomId(chatRoom.getId())
                .chatRoomName(chatRoomName)
                .userResponses(allUsers)
                .build();

        // 로그인한 유저 및 참여 유저들에게 메시지 전송
        for (ChatRoomUserResponse userResponse : allUsers) {
            messagingTemplate.convertAndSend("/queue/chatroom/" + userResponse.getUserNickname(), response);
        }
        userChatRoomRepository.saveAll(userChatRooms);
    }

    // 특정 채팅방에 로그인된 유저와 다른 유저들이 있는지 확인
    @Transactional
    private Optional<ChatRoom> findChatRoomWithUsers(String logInUserId, String[] nicknames) {
        // 닉네임 배열과 로그인 사용자 닉네임을 포함한 집합
        Set<String> nicknameSet = new HashSet<>(Arrays.asList(nicknames));

        // 모든 유저를 포함해야 하므로 로그인 유저의 닉네임을 추가
        User logInUser = userRepository.findByLoginId(logInUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 로그인 ID가 없습니다: " + logInUserId));
        nicknameSet.add(logInUser.getNickname());

        // 사용자가 속한 모든 채팅방을 가져옴
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserLoginId(logInUserId);

        for (UserChatRoom userChatRoom : userChatRooms) {
            ChatRoom chatRoom = userChatRoom.getChatRoom();

            // 해당 채팅방에 속한 모든 유저의 닉네임을 가져옴
            List<String> usersInChatRoomNicknames = userChatRoomRepository.findByChatRoomId(chatRoom.getId()).stream()
                    .map(ucr -> ucr.getUser().getNickname())
                    .collect(Collectors.toList());

            // 채팅방의 사용자 집합과 주어진 닉네임 집합을 비교
            Set<String> usersInChatRoomSet = new HashSet<>(usersInChatRoomNicknames);

            // 사용자 집합이 동일하고, 채팅방 내 유저 수와 주어진 닉네임 수가 동일할 경우에만 동일한 채팅방으로 간주
            if (usersInChatRoomSet.equals(nicknameSet) && usersInChatRoomSet.size() == nicknameSet.size()) {
                return Optional.of(chatRoom);
            }
        }
        return Optional.empty();
    }

    // 채팅방에 유저 초대
    @Transactional
    public void inviteUser(Long chatRoomId, String[] nicknames) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 채팅방 ID가 없습니다."));

        List<UserChatRoom> existingUsers = userChatRoomRepository.findByChatRoomId(chatRoomId);
        Set<String> existingNicknames = existingUsers.stream()
                .map(ucr -> ucr.getUser().getNickname())
                .collect(Collectors.toSet());

        List<ChatRoomUserResponse> allUsers = existingUsers.stream()
                .map(ucr -> ChatRoomUserResponse.builder()
                        .userNickname(ucr.getUser().getNickname())
                        .profileImageUrl(ucr.getUser().getProfileImageUrl())
                        .build())
                .collect(Collectors.toList());

        List<ChatRoomUserResponse> newUsers = new ArrayList<>();
        List<UserChatRoom> userChatRooms = new ArrayList<>();

        for (String nickname : nicknames) {
            if (existingNicknames.contains(nickname)) {
                continue; // 이미 존재하는 유저는 제외
            }

            User user = userRepository.findByNickname(nickname)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 닉네임이 없습니다: " + nickname));

            newUsers.add(ChatRoomUserResponse.builder()
                    .userNickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build());

            userChatRooms.add(UserChatRoom.builder()
                    .user(user)
                    .chatRoom(chatRoom)
                    .build());
        }

        if (!newUsers.isEmpty()) {
            // 새로운 유저들을 채팅방 유저 목록에 추가
            allUsers.addAll(newUsers);

            // 채팅방 이름 업데이트
            String newNicknames = newUsers.stream()
                    .map(ChatRoomUserResponse::getUserNickname)
                    .collect(Collectors.joining(", "));
            chatRoom.setName(String.join(", ", existingNicknames) + ", " + newNicknames);
            chatRoomRepository.save(chatRoom);

            InviteChatRoomResponse response = InviteChatRoomResponse.builder()
                    .messageType(MessageType.INVITE)
                    .chatRoomId(chatRoom.getId())
                    .userResponses(allUsers)
                    .build();

            for (ChatRoomUserResponse userResponse : allUsers) {
                messagingTemplate.convertAndSend("/queue/chatroom/" + userResponse.getUserNickname(), response);
            }
            alertMessage(chatRoomId, newNicknames + "님이 초대되었습니다.");
        }
        userChatRoomRepository.saveAll(userChatRooms);
    }

    // 채팅방 이름 업데이트
    @Transactional
    private void updateChatRoomName(ChatRoom chatRoom, String[] nicknames) {
        String updatedName = chatRoom.getUserChatRooms().stream()
                .map(userChatRoom -> userChatRoom.getUser().getNickname())
                .collect(Collectors.joining(", "));
        chatRoom.setName(updatedName);
        chatRoomRepository.save(chatRoom);
    }

    // 채팅방 나가기
    @Transactional
    public void leaveChatRoom(Long chatRoomId, String logInUserId) {
        User logInUser = userRepository.findByLoginId(logInUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 로그인 ID가 없습니다: " + logInUserId));

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(logInUser.getId(), chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 채팅방에 사용자가 존재하지 않습니다."));

        // 해당 채팅방의 유저 수를 확인
        List<UserChatRoom> chatRoomUsers = userChatRoomRepository.findByChatRoomId(chatRoomId);

        LeaveRoomResponse response = LeaveRoomResponse.builder()
                .messageType(MessageType.LEAVE)
                .chatRoomId(chatRoomId)
                .build();

        if (chatRoomUsers.size() == 1) {
            // 채팅방에 유저가 1명인 경우 채팅방 자체를 삭제
            userChatRoomRepository.delete(userChatRoom);
            chatRoomRepository.deleteById(chatRoomId);
            messagingTemplate.convertAndSend("/queue/chatroom/" + logInUser.getNickname(), response);
        } else {
            // 그렇지 않은 경우, 해당 유저만 삭제
            userChatRoomRepository.delete(userChatRoom);
            alertMessage(chatRoomId, logInUser.getNickname() + "님이 나갔습니다.");

            messagingTemplate.convertAndSend("/queue/chatroom/" + logInUser.getNickname(), response);

            // 나머지 유저들의 정보 생성
            List<ChatRoomUserResponse> remainingUsers = chatRoomUsers.stream()
                    .filter(ucr -> !ucr.getUser().getId().equals(logInUser.getId())) // 나간 유저 제외
                    .map(ucr -> ChatRoomUserResponse.builder()
                            .userNickname(ucr.getUser().getNickname())
                            .profileImageUrl(ucr.getUser().getProfileImageUrl())
                            .build())
                    .collect(Collectors.toList());

            response.setUserResponses(remainingUsers);
            messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, response);
        }
    }

    // 채팅방 목록 조회
    @Transactional
    public List<ChatRoomListResponse> getChatRoomList(String logInUserId) {
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserLoginId(logInUserId);

        return userChatRooms.stream()
                .map(userChatRoom -> {
                    ChatRoom chatRoom = userChatRoom.getChatRoom();
                    List<UserChatRoom> usersInChatRoom = userChatRoomRepository.findByChatRoomId(chatRoom.getId());

                    List<ChatRoomUserResponse> userResponses = usersInChatRoom.stream()
                            .map(ucr -> ChatRoomUserResponse.builder()
                                    .userNickname(ucr.getUser().getNickname())
                                    .profileImageUrl(ucr.getUser().getProfileImageUrl())
                                    .build())
                            .collect(Collectors.toList());

                    return ChatRoomListResponse.builder()
                            .chatRoomId(chatRoom.getId())
                            .chatRoomName(chatRoom.getName())
                            .userResponses(userResponses)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 채팅방 이름 업데이트
    @Transactional
    public void updateChatRoomName(Long chatRoomId, String logInUserId, String newChatRoomName) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 채팅방 ID가 없습니다: " + chatRoomId));

        chatRoom.setName(newChatRoomName);
        chatRoomRepository.save(chatRoom);

        UpdateRoomNameResponse response = UpdateRoomNameResponse.builder()
                .messageType(MessageType.NAME_UPDATE)
                .chatRoomId(chatRoomId)
                .chatRoomName(newChatRoomName)
                .build();

        messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, response);
    }

    // 특정 채팅방에 있는 모든 유저들의 닉네임 리스트 반환
    @Transactional
    public List<String> getUserNicknamesInChatRoom(Long chatRoomId, String loginUserId) {
        List<UserChatRoom> chatRoomUsers = userChatRoomRepository.findByChatRoomId(chatRoomId);
        return chatRoomUsers.stream()
                .filter(userChatRoom -> !userChatRoom.getUser().getLoginId().equals(loginUserId))
                .map(userChatRoom -> userChatRoom.getUser().getNickname())
                .collect(Collectors.toList());
    }

    // 닉네임에 따른 모든 채팅방Id 반환
    @Transactional
    public List<Long> getChatRoomIdsByLoginUserId(String logInUserId) {
        return userChatRoomRepository.findByUserLoginId(logInUserId).stream()
                .map(userChatRoom -> userChatRoom.getChatRoom().getId())
                .collect(Collectors.toList());
    }

    // 예외 처리 개선 예시
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    // 알림 메시지
    private void alertMessage(Long chatRoomId, String content) {
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .sender("admin")
                .content(content)
                .build();
        ZSetOperations<String, ChatMessage> zSetOps = redisTemplate.opsForZSet();
        String key = CHAT_ROOM_MESSAGES_PREFIX + chatRoomId;
        long currentTimeMillis = System.currentTimeMillis();
        double score = (double) currentTimeMillis;

        // redis에 채팅방 ID를 기반으로 키를 생성하고, score를 사용하여 저장합니다.
        zSetOps.add(key, chatMessage, score);
        // TTL 설정: 5일 후에 자동으로 데이터 삭제
        redisTemplate.expire(key, 5, TimeUnit.DAYS);

        // 날짜 형식 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date(currentTimeMillis));

        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageType(MessageType.CHAT)
                .chatRoomId(chatMessage.getChatRoomId())
                .sender(chatMessage.getSender())
                .content(chatMessage.getContent())
                .timestamp(formattedDate)
                .build();

        messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, response);
    }
}
