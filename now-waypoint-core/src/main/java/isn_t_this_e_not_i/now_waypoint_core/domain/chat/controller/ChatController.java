package isn_t_this_e_not_i.now_waypoint_core.domain.chat.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.request.CreateMessageRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.ChatMessageResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.UpdateInfoResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.request.CreateChatRoomRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.request.InviteUserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.request.UpdateChatRoomNameRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response.ChatRoomListResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response.ChatRoomResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response.ChatRoomsInfoResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response.UpdateChatRoomNameResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.service.ChatMessageService;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.service.UserChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final UserChatRoomService userChatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // STOMP 메시지 핸들러
    @MessageMapping("/{chatRoomId}/chat/message")
    @Operation(summary = "채팅 메시지 전송", description = "지정된 채팅방에 채팅 메시지를 전송합니다.")
    public void sendMessage(@PathVariable Long chatRoomId, @Payload CreateMessageRequest createMessageRequest, Principal principal) {
        ChatMessageResponse response = chatMessageService.saveMessage(principal.getName(), chatRoomId, createMessageRequest.getContent());
        // 채팅방의 모든 클라이언트에게 메시지 전송
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, response);
    }

    @GetMapping("/chatRoom/{chatRoomId}/messages/recent")
    @Operation(summary = "최근 메시지 조회", description = "특정 채팅방의 최근 메시지를 조회합니다.")
    public ResponseEntity<List<ChatMessageResponse>> getRecentMessages(@PathVariable Long chatRoomId, @RequestParam int count, Principal principal) {
        List<ChatMessageResponse> messages = chatMessageService.getRecentMessages(chatRoomId, principal.getName(), count);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/chatRoom/{chatRoomId}/messages/before")
    @Operation(summary = "이전 메시지 조회", description = "특정 채팅방에서 주어진 최대 score 값보다 낮은 메시지를 조회합니다.")
    public ResponseEntity<List<ChatMessageResponse>> getMessagesBefore(@PathVariable Long chatRoomId, @RequestParam double maxScore, @RequestParam int count) {
        List<ChatMessageResponse> messages = chatMessageService.getMessagesBefore(chatRoomId, maxScore, count);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/create")
    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    public ResponseEntity<ChatRoomResponse> createChatRoom(@RequestBody CreateChatRoomRequest request, Principal principal) {
        ChatRoomResponse response = userChatRoomService.createChatRoom(principal.getName(), request.getNicknames(), false);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/create/duplicate")
    @Operation(summary = "중복 채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    public ResponseEntity<ChatRoomResponse> createDuplicateChatRoom(@RequestBody CreateChatRoomRequest request, Principal principal) {
        ChatRoomResponse response = userChatRoomService.createChatRoom(principal.getName(), request.getNicknames(), true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/chatRoom/{chatRoomId}/invite")
    @Operation(summary = "채팅방에 유저 초대", description = "기존 채팅방에 유저를 초대합니다.")
    public ResponseEntity<String> inviteUser(@PathVariable Long chatRoomId, @RequestBody InviteUserRequest inviteUserRequest) {
        userChatRoomService.inviteUser(chatRoomId, inviteUserRequest.getNicknames());
        return ResponseEntity.ok("유저가 성공적으로 초대되었습니다.");
    }

    @PostMapping("/chatRoom/{chatRoomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나갑니다.")
    public ResponseEntity<String> leaveChatRoom(@PathVariable Long chatRoomId, Principal principal) {
        userChatRoomService.leaveChatRoom(chatRoomId, principal.getName());
        return ResponseEntity.ok("사용자가 채팅방을 나갔습니다.");
    }

    @GetMapping("/list")
    @Operation(summary = "유저의 채팅방 목록 조회", description = "로그인한 유저의 채팅방 목록을 조회합니다.")
    public ResponseEntity<ChatRoomsInfoResponse> getChatRoomsForUser(Principal principal) {
        String logInUserId = principal.getName();
        List<ChatRoomListResponse> chatRooms = userChatRoomService.getChatRoomsForUser(logInUserId);
        List<UpdateInfoResponse> chatRoomsInfoByUser = chatMessageService.getChatRoomsInfoByUser(logInUserId);
        // 커스텀 클래스에 담아서 반환
        ChatRoomsInfoResponse response = new ChatRoomsInfoResponse(chatRooms, chatRoomsInfoByUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/chatRoom/{chatRoomId}/update")
    @Operation(summary = "채팅방 업데이트 정보 조회", description = "특정 채팅방의 업데이트 정보를 조회합니다.")
    public ResponseEntity<UpdateInfoResponse> getChatRoomUpdate(@PathVariable Long chatRoomId, Principal principal) {
        String logInUserId = principal.getName();
        UpdateInfoResponse chatRoomInfo = chatMessageService.getChatRoomInfo(logInUserId, chatRoomId);
        return ResponseEntity.ok(chatRoomInfo);
    }

    @PutMapping("/chatRoom/{chatRoomId}/nameUpdate")
    @Operation(summary = "채팅방 이름 변경", description = "채팅방의 이름을 변경합니다.")
    public ResponseEntity<UpdateChatRoomNameResponse> updateChatRoomName(@PathVariable Long chatRoomId, @RequestBody UpdateChatRoomNameRequest updateChatRoomNameRequest, Principal principal) {
        UpdateChatRoomNameResponse response = userChatRoomService.updateChatRoomName(chatRoomId, principal.getName(), updateChatRoomNameRequest.getNewChatRoomName());
        return ResponseEntity.ok(response);
    }
}