package isn_t_this_e_not_i.now_waypoint_core.domain.chat.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.request.CreateMessageRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.ChatMessageResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.UpdateInfoResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
    public ChatMessageResponse saveMessage(String loginUserId, Long chatRoomId, String content) {
        String sender = userRepository.findByLoginId(loginUserId).get().getNickname();
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .sender(sender)
                .content(content)
                .build();

        ZSetOperations<String, ChatMessage> zSetOps = redisTemplate.opsForZSet();
        String key = CHAT_ROOM_MESSAGES_PREFIX + chatRoomId;
        double score = System.currentTimeMillis(); // timestamp 대신 score 사용

        // redis에 채팅방 ID를 기반으로 키를 생성하고, score를 사용하여 저장합니다.
        zSetOps.add(key, chatMessage, score);

        // TTL 설정: 5일 후에 자동으로 데이터 삭제
        redisTemplate.expire(key, 5, TimeUnit.DAYS);

        // 마지막 메시지 저장(내용/시간)
        String lastMessageKey = LAST_MESSAGE_PREFIX + chatRoomId;
        redisTemplate.opsForHash().put(lastMessageKey, "content", chatMessage.getContent());
        redisTemplate.opsForHash().put(lastMessageKey, "timestamp", String.valueOf(score));

        // 채팅방의 모든 사용자에게 안 읽은 메시지 수 증가
        userChatRoomService.getUserNicknamesInChatRoom(chatRoomId, loginUserId).stream()
                .forEach(userNickname -> {
                    String unreadKey = UNREAD_MESSAGES_PREFIX + userNickname + ":" + chatRoomId;
                    redisStringTemplate.opsForValue().increment(unreadKey);

                    // 각 사용자에게 업데이트 메시지 보내기
                    messagingTemplate.convertAndSend("/queue/chatroom/" + userNickname, "New message in chat room : " + chatRoomId);
                });

        // DTO 객체 반환
        return ChatMessageResponse.builder()
                .chatRoomId(chatMessage.getChatRoomId())
                .sender(chatMessage.getSender())
                .content(chatMessage.getContent())
                .timestamp(String.valueOf(score))
                .build();
    }

    /**
     * 채팅방의 모든 메시지 조회
     *  •	chatRoomId: 조회할 채팅방의 ID.
     * 	•	logInUserId: 사용자의 닉네임으로 변환. 해당 사용자의 안 읽은 메시지 개수를 설정하는 용도로 사용됩니다.
     * 	•	count: 조회할 최신 메시지의 개수.
     * @return 채팅방의 모든 메시지 목록
     */
    public List<ChatMessageResponse> getRecentMessages(Long chatRoomId, String logInUserId, int count) {
        String userNickname = userRepository.findByLoginId(logInUserId).get().getNickname();
        String key = CHAT_ROOM_MESSAGES_PREFIX + chatRoomId;
        // ZSet (Sorted Set)에서 주어진 범위의 메시지를 내림차순으로 조회합니다.
        Set<ZSetOperations.TypedTuple<ChatMessage>> messages = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, count - 1);

        // TypedTuple에서 ChatMessage 객체만 추출하여 리스트로 반환
        List<ChatMessageResponse> recentMessages = messages.stream()
                .map(typedTuple -> ChatMessageResponse.builder()
                        .chatRoomId(typedTuple.getValue().getChatRoomId())
                        .sender(typedTuple.getValue().getSender())
                        .content(typedTuple.getValue().getContent())
                        .timestamp(String.valueOf(typedTuple.getScore()))
                        .build())
                .collect(Collectors.toList());

        // 해당 사용자의 안 읽은 메시지 개수를 0으로 설정
        String unreadKey = UNREAD_MESSAGES_PREFIX + userNickname + ":" + chatRoomId;
        redisStringTemplate.opsForValue().set(unreadKey, "0");

        return recentMessages;
    }

    /**
     *  •	chatRoomId: 조회할 채팅방의 ID.
     * 	•	maxScore: 조회할 메시지의 최대 score 값. 이 값보다 낮은 메시지들을 조회합니다.
     * 	•	count: 조회할 메시지의 개수.
     */
    public List<ChatMessageResponse> getMessagesBefore(Long chatRoomId, double maxScore, int count) {
        String key = CHAT_ROOM_MESSAGES_PREFIX + chatRoomId;
        // maxScore에서 Double.NEGATIVE_INFINITY까지 점수가 내림차순으로 정렬된 메시지를 count만큼 조회
        Set<ZSetOperations.TypedTuple<ChatMessage>> messages = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, Double.NEGATIVE_INFINITY, maxScore, 0, count);

        // TypedTuple에서 ChatMessage 객체를 DTO로 매핑하여 리스트로 반환
        List<ChatMessageResponse> messagesBefore = messages.stream()
                .map(typedTuple -> ChatMessageResponse.builder()
                        .chatRoomId(typedTuple.getValue().getChatRoomId())
                        .sender(typedTuple.getValue().getSender())
                        .content(typedTuple.getValue().getContent())
                        .timestamp(String.valueOf(typedTuple.getScore()))
                        .build())
                .collect(Collectors.toList());

        return messagesBefore;
    }

    // 업데이트되는 전체 채팅방 정보
    public List<UpdateInfoResponse> getChatRoomsInfoByUser(String logInUserId) {
        String userNickname = userRepository.findByLoginId(logInUserId).get().getNickname();
        List<Long> chatRoomIds = userChatRoomService.getChatRoomIdsByUserNickname(logInUserId);
        List<UpdateInfoResponse> chatRoomInfos = new ArrayList<>();

        for (Long chatRoomId : chatRoomIds) {
            int unreadMessagesCount = getUnreadMessagesCount(userNickname, chatRoomId);
            Map<String, String> lastMessageInfo = getLastMessageInfo(chatRoomId);

            UpdateInfoResponse chatRoomInfo = UpdateInfoResponse.builder()
                    .chatRoomId(chatRoomId)
                    .unreadMessagesCount(unreadMessagesCount)
                    .lastMessageContent(lastMessageInfo.get("content"))
                    .lastMessageTimestamp(lastMessageInfo.get("timestamp"))
                    .build();

            chatRoomInfos.add(chatRoomInfo);
        }

        return chatRoomInfos;
    }

    public UpdateInfoResponse getChatRoomInfo(String logInUserId, Long chatRoomId) {
        String userNickname = userRepository.findByLoginId(logInUserId).get().getNickname();
        Map<String, String> lastMessageInfo = getLastMessageInfo(chatRoomId);

        int unreadMessagesCount = getUnreadMessagesCount(userNickname, chatRoomId);
        UpdateInfoResponse chatRoomInfo = UpdateInfoResponse.builder()
                .chatRoomId(chatRoomId)
                .unreadMessagesCount(unreadMessagesCount)
                .lastMessageContent(lastMessageInfo.get("content"))
                .lastMessageTimestamp(lastMessageInfo.get("timestamp"))
                .build();
        return chatRoomInfo;
    }

    private int getUnreadMessagesCount(String logInUserId, Long chatRoomId) {
        String userNickname = userRepository.findByLoginId(logInUserId).get().getNickname();
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
