package isn_t_this_e_not_i.now_waypoint_core.domain.chat.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.MessageType;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.ChatMessageListResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.ChatMessageResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.UpdateInfoResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;
    private final UserChatRoomService userChatRoomService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // SimpMessagingTemplate 주입

    private static final String CHAT_ROOM_MESSAGES_PREFIX = "chatroom:messages:";
    private static final String LAST_MESSAGE_PREFIX = "lastMessage:";
    private static final String UNREAD_MESSAGES_PREFIX = "unreadMessages:";


    /**
     * 채팅 메시지 생성
     * @Request : Long chatRoomId, String loginUserId, String content
     * @Response : String sender, String content, LocalDateTime timestamp;
     */
    public void saveMessage(String loginUserId, Long chatRoomId, String content) {
        String sender = userRepository.findByLoginId(loginUserId).get().getNickname();
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .sender(sender)
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
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String formattedDate = dateFormat.format(new Date(currentTimeMillis));

        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageType(MessageType.CHAT)
                .chatRoomId(chatMessage.getChatRoomId())
                .sender(chatMessage.getSender())
                .content(chatMessage.getContent())
                .timestamp(formattedDate)
                .build();

        // 마지막 메시지 저장(내용/시간)
        String lastMessageKey = LAST_MESSAGE_PREFIX + chatRoomId;
        redisStringTemplate.opsForHash().put(lastMessageKey, "content", chatMessage.getContent());
        redisStringTemplate.opsForHash().put(lastMessageKey, "timestamp", formattedDate);

        // 채팅방의 모든 사용자에게 안 읽은 메시지 수 증가
        userChatRoomService.getUserNicknamesInChatRoom(chatRoomId, loginUserId).stream()
                .forEach(userNickname -> {
                    String unreadKey = UNREAD_MESSAGES_PREFIX + userNickname + ":" + chatRoomId;
                    redisStringTemplate.opsForValue().increment(unreadKey);

                    // 각 사용자에게 업데이트 메시지 보내기
                    messagingTemplate.convertAndSend("/queue/chatroom/" + userNickname, response);
                });
        messagingTemplate.convertAndSend("/queue/chatroom/" + sender, response);
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, response);
    }

    /**
     * 채팅방의 모든 메시지 조회
     *  •	chatRoomId: 조회할 채팅방의 ID.
     * 	•	logInUserId: 사용자의 닉네임으로 변환. 해당 사용자의 안 읽은 메시지 개수를 설정하는 용도로 사용됩니다.
     * @return 채팅방의 모든 메시지 목록
     */
    public void getRecentMessages(Long chatRoomId, String logInUserId) {
        String userNickname = userRepository.findByLoginId(logInUserId).get().getNickname();
        String key = CHAT_ROOM_MESSAGES_PREFIX + chatRoomId;
        // ZSet (Sorted Set)에서 주어진 범위의 메시지를 내림차순으로 조회합니다.
        Set<ZSetOperations.TypedTuple<ChatMessage>> messages = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 49); // count를 50으로 고정

        // 날짜 형식 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        // TypedTuple에서 ChatMessage 객체만 추출하여 리스트로 반환
        List<ChatMessageResponse> recentMessages = messages.stream()
                .map(typedTuple -> {
                    String formattedDate = dateFormat.format(new Date(typedTuple.getScore().longValue()));
                    return ChatMessageResponse.builder()
                            .sender(typedTuple.getValue().getSender())
                            .content(typedTuple.getValue().getContent())
                            .timestamp(formattedDate)
                            .build();
                })
                .collect(Collectors.toList());

        ChatMessageListResponse response = ChatMessageListResponse.builder()
                .messageType(MessageType.CHAT_LIST)
                .messages(recentMessages)
                .build();

        // 해당 사용자의 안 읽은 메시지 개수를 0으로 설정
        String unreadKey = UNREAD_MESSAGES_PREFIX + userNickname + ":" + chatRoomId;
        redisStringTemplate.opsForValue().set(unreadKey, "0");

        messagingTemplate.convertAndSend("/queue/chatroom/" + userNickname, response);
    }

    /**
     *  •	chatRoomId: 조회할 채팅방의 ID.
     *  .   이전까지 읽었던 메시지score
     */
    public void getMessagesBefore(Long chatRoomId, String logInUserId, String timestamp) {
        String userNickname = userRepository.findByLoginId(logInUserId).get().getNickname();
        double maxScore = Double.parseDouble(timestamp); // String을 double로 변환

        String key = CHAT_ROOM_MESSAGES_PREFIX + chatRoomId;
        // maxScore에서 Double.NEGATIVE_INFINITY까지 점수가 내림차순으로 정렬된 메시지를 50개 조회
        Set<ZSetOperations.TypedTuple<ChatMessage>> messages = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, Double.NEGATIVE_INFINITY, maxScore, 0, 50); // count를 50으로 고정

        // TypedTuple에서 ChatMessage 객체를 DTO로 매핑하여 리스트로 반환
        List<ChatMessageResponse> messagesBefore = messages.stream()
                .map(typedTuple -> ChatMessageResponse.builder()
                        .sender(typedTuple.getValue().getSender())
                        .content(typedTuple.getValue().getContent())
                        .timestamp(String.valueOf(typedTuple.getScore()))
                        .build())
                .collect(Collectors.toList());

        ChatMessageListResponse response = ChatMessageListResponse.builder()
                .messageType(MessageType.CHAT_LIST)
                .messages(messagesBefore)
                .build();

        // 해당 사용자의 안 읽은 메시지 개수를 0으로 설정
        String unreadKey = UNREAD_MESSAGES_PREFIX + userNickname + ":" + chatRoomId;
        redisStringTemplate.opsForValue().set(unreadKey, "0");

        messagingTemplate.convertAndSend("/queue/chatroom/" + userNickname, response);
    }

    // 전체 채팅방의 업데이트 정보를 조회
    public List<UpdateInfoResponse> getChatRoomsInfoByUser(String logInUserId) {
        List<Long> chatRoomIds = userChatRoomService.getChatRoomIdsByLoginUserId(logInUserId);
        List<UpdateInfoResponse> updateInfoResponses = new ArrayList<>();

        for (Long chatRoomId : chatRoomIds) {
            int unreadMessagesCount = getUnreadMessagesCount(logInUserId, chatRoomId);
            Map<String, String> lastMessageInfo = getLastMessageInfo(chatRoomId);

            UpdateInfoResponse chatRoomInfo = UpdateInfoResponse.builder()
                    .chatRoomId(chatRoomId)
                    .unreadMessagesCount(unreadMessagesCount)
                    .lastMessageContent(lastMessageInfo.get("content"))
                    .lastMessageTimestamp(lastMessageInfo.get("timestamp"))
                    .build();

            updateInfoResponses.add(chatRoomInfo);
        }

        return updateInfoResponses;
    }

    // 특정 채팅방의 업데이트 정보를 조회
    public void getChatRoomInfo(String logInUserId, Long chatRoomId) {
        String userNickname = userRepository.findByLoginId(logInUserId).get().getNickname();
        Map<String, String> lastMessageInfo = getLastMessageInfo(chatRoomId);
        int unreadMessagesCount = getUnreadMessagesCount(userNickname, chatRoomId);

        UpdateInfoResponse chatRoomInfo = UpdateInfoResponse.builder()
                .messageType(MessageType.UPDATE)
                .chatRoomId(chatRoomId)
                .unreadMessagesCount(unreadMessagesCount)
                .lastMessageContent(lastMessageInfo.get("content"))
                .lastMessageTimestamp(lastMessageInfo.get("timestamp"))
                .build();

        messagingTemplate.convertAndSend("/queue/chatroom/" + userNickname, chatRoomInfo);
    }

    @Transactional
    public void updateUserNicknameInMessages(String oldNickname, String newNickname, String loginUserId) {
        List<Long> chatRoomIds = userChatRoomService.getChatRoomIdsByLoginUserId(loginUserId);
        for (Long chatRoomId : chatRoomIds) {
            String key = CHAT_ROOM_MESSAGES_PREFIX + chatRoomId;
            Set<ZSetOperations.TypedTuple<ChatMessage>> messages = redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

            if (messages != null) {
                List<ChatMessage> messagesToRemove = new ArrayList<>();
                messages.forEach(typedTuple -> {
                    ChatMessage message = typedTuple.getValue();
                    if (message.getSender().equals(oldNickname)) {
                        messagesToRemove.add(message);
                    }
                });
                messagesToRemove.forEach(message -> redisTemplate.opsForZSet().remove(key, message));

                messages.forEach(typedTuple -> {
                    ChatMessage message = typedTuple.getValue();
                    if (message.getSender().equals(oldNickname)) {
                        message.setSender(newNickname);
                    }
                    redisTemplate.opsForZSet().add(key, message, typedTuple.getScore());
                });
            }
        }
    }


    private int getUnreadMessagesCount(String userNickname, Long chatRoomId) {
        String unreadKey = UNREAD_MESSAGES_PREFIX + userNickname + ":" + chatRoomId;
        String count = redisStringTemplate.opsForValue().get(unreadKey);
        return count != null ? Integer.parseInt(count) : 0;
    }

    private Map<String, String> getLastMessageInfo(Long chatRoomId) {
        String lastMessageKey = LAST_MESSAGE_PREFIX + chatRoomId;
        Map<Object, Object> lastMessageInfo = redisStringTemplate.opsForHash().entries(lastMessageKey);

        Map<String, String> result = new HashMap<>();
        result.put("content", (String) lastMessageInfo.get("content"));
        result.put("timestamp", (String) lastMessageInfo.get("timestamp"));

        return result;
    }
}
