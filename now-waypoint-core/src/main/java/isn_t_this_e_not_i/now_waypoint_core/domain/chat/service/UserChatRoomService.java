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

    /**
     * 채팅방 생성 -> /queue/update/{userNickName} 으로 채팅방 업데이트 웹소켓 메시지 전송
     * @Request : String logInUserId, String[] nickNames, boolean allowDuplicates
     * @Response : Long chatRoomId, String ChatRoomName, List<userResponses>
     */
    @Transactional
    public void createChatRoom(String logInUserId, String[] nicknames) {
        User logInUser = userRepository.findByLoginId(logInUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 로그인 ID가 없습니다: " + logInUserId));
        String logInUserNickname = logInUser.getNickname();

        // 로그인 유저의 닉네임이 nicknames 배열에 포함되어 있는지 체크
        boolean hasDuplicateNickname = Arrays.stream(nicknames)
                .anyMatch(nickname -> nickname.equals(logInUserNickname));
        if (hasDuplicateNickname) {
            ErrorMessageResponse response = ErrorMessageResponse.builder()
                    .messageType(MessageType.ERROR)
                    .content("나와의 채팅방 생성에 실패했습니다.")
                    .build();
            messagingTemplate.convertAndSend("/queue/chatroom/" + logInUserNickname, response);
            throw new IllegalArgumentException("나와의 채팅방 생성에 실패했습니다.");
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

        final ChatRoom chatRoom = ChatRoom.builder().name(chatRoomName).build();
        chatRoomRepository.save(chatRoom);

        // 닉네임에 해당하는 유저 정보 수집 및 ChatRoomUserResponse로 변환
        List<ChatRoomUserResponse> allUsers = Arrays.stream(nicknames)
                .map(nickname -> userRepository.findByNickname(nickname)
                        .orElseThrow(() -> new IllegalArgumentException("해당하는 닉네임이 없습니다: " + nickname)))
                .map(user -> ChatRoomUserResponse.builder()
                        .userNickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .collect(Collectors.toList());

        allUsers.add(ChatRoomUserResponse.builder()
                .userNickname(logInUserNickname)
                .profileImageUrl(logInUser.getProfileImageUrl())
                .build());

        CreateChatRoomResponse response = CreateChatRoomResponse.builder()
                .messageType(MessageType.CREATE)
                .chatRoomId(chatRoom.getId())
                .chatRoomName(chatRoomName)
                .userResponses(allUsers)
                .build();

        List<UserChatRoom> userChatRooms = Arrays.stream(nicknames)
                .map(nickname -> userRepository.findByNickname(nickname)
                        .orElseThrow(() -> new IllegalArgumentException("해당하는 닉네임이 없습니다: " + nickname)))
                .map(user -> {
                    UserChatRoom userChatRoom = UserChatRoom.builder()
                            .user(user)
                            .chatRoom(chatRoom)
                            .build();

                    // 유저에게 새로운 채팅방 생성 메시지 전송
                    messagingTemplate.convertAndSend("/queue/chatroom/" + user.getNickname(), response);

                    return userChatRoom;
                })
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/queue/chatroom/" + logInUserNickname, response);
        // 로그인한 유저도 채팅방에 추가
        UserChatRoom logInUserChatRoom = UserChatRoom.builder()
                .user(logInUser)
                .chatRoom(chatRoom)
                .build();
        userChatRooms.add(logInUserChatRoom);

        userChatRoomRepository.saveAll(userChatRooms);
    }

    // 특정 채팅방에 로그인된 유저와 다른 유저들이 있는지 확인
    @Transactional
    private Optional<ChatRoom> findChatRoomWithUsers(String logInUserId, String[] nicknames) {
        User logInUser = userRepository.findByLoginId(logInUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 로그인 ID가 없습니다: " + logInUserId));
        String logInUserNickname = logInUser.getNickname();

        // 닉네임 배열을 Set으로 변환하여 빠른 검색을 가능하게 함
        Set<String> nicknameSet = new HashSet<>(Arrays.asList(nicknames));

        // 로그인 유저의 닉네임을 닉네임 세트에서 제거
        nicknameSet.remove(logInUserNickname);

        // 로그인 유저가 참여하고 있는 채팅방 조회
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserLoginId(logInUserId);

        // 모든 참여자들의 채팅방 아이디를 찾아 리스트로 만듦
        for (UserChatRoom userChatRoom : userChatRooms) {
            ChatRoom chatRoom = userChatRoom.getChatRoom();
            List<String> usersInChatRoomNicknames = userChatRoomRepository.findByChatRoomId(chatRoom.getId()).stream()
                    .map(ucr -> ucr.getUser().getNickname())
                    .collect(Collectors.toList());

            // 로그인한 유저의 닉네임을 채팅방 사용자 닉네임 리스트에서 제거
            usersInChatRoomNicknames.remove(logInUserNickname);

            // usersInChatRoomNicknames과 nicknameSet의 비교
            Set<String> chatRoomNicknamesSet = new HashSet<>(usersInChatRoomNicknames);

            if (chatRoomNicknamesSet.equals(nicknameSet)) {
                return Optional.of(chatRoom); // 모든 닉네임이 일치하는 채팅방 발견
            }
        }
        return Optional.empty(); // 일치하는 채팅방 없음
    }

    /**
     * 채팅방에 유저 초대
     * @Request : Long chatRoomId, String[] nicknames
     */
    @Transactional
    public void inviteUser(Long chatRoomId, String[] nicknames) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 채팅방 ID가 없습니다."));

        List<ChatRoomUserResponse> allUsers = Arrays.stream(nicknames)
                .map(nickname -> userRepository.findByNickname(nickname)
                        .orElseThrow(() -> new IllegalArgumentException("해당하는 닉네임이 없습니다: " + nickname)))
                .map(user -> ChatRoomUserResponse.builder()
                        .userNickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .collect(Collectors.toList());

        List<UserChatRoom> userChatRooms = Arrays.stream(nicknames)
                .map(nickname -> userRepository.findByNickname(nickname)
                        .orElseThrow(() -> new IllegalArgumentException("해당하는 닉네임이 없습니다: " + nickname)))
                .map(user -> UserChatRoom.builder()
                        .user(user)
                        .chatRoom(chatRoom)
                        .build())
                .collect(Collectors.toList());
        userChatRoomRepository.saveAll(userChatRooms);

        // 채팅방 이름 업데이트
        updateChatRoomName(chatRoom, nicknames);

        InviteChatRoomResponse response = InviteChatRoomResponse.builder()
                .messageType(MessageType.INVITE)
                .chatRoomId(chatRoom.getId())
                .userResponses(allUsers)
                .build();

        alertMessage(chatRoomId, Arrays.stream(nicknames).collect(Collectors.joining(", ")) + "님이 초대되었습니다.");

        Arrays.stream(nicknames)
                .forEach(nickname ->
                        messagingTemplate.convertAndSend("/queue/chatroom/" + nickname, response)
                );
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

    /**
     * 채팅방 나가기
     * @Request : String loginId, Long chatRoomId
     */
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

    /**
     * 채팅방 목록 조회
     * @Request : String logInUserId
     * @Response : Long chatRoomId, String chatRoomName, Long 채팅방에 있는 유저의 수
     */
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

    /**
     * @Request : String logInUserId, Long chatRoomId, String newChatRoomName
     */
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
