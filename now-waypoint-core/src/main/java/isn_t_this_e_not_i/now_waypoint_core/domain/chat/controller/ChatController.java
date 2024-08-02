package isn_t_this_e_not_i.now_waypoint_core.domain.chat.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.request.ChatMessageBeforeRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.request.ChatRoomIdRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.request.CreateMessageRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.ChatMessageResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.UpdateInfoResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.request.CreateChatRoomRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.request.InviteUserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.request.UpdateRoomNameRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response.ChatRoomListResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response.ChatRoomsInfoResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.service.ChatMessageService;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.service.UserChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/list")
    @Operation(summary = "유저의 채팅방 목록 조회", description = "로그인한 유저의 채팅방 목록을 조회합니다.")
    public ResponseEntity<ChatRoomsInfoResponse> getChatRooms(Authentication auth) {
        String logInUserId = auth.getName();
        List<ChatRoomListResponse> chatRooms = userChatRoomService.getChatRoomsForUser(logInUserId);
        List<UpdateInfoResponse> chatRoomsInfoByUser = chatMessageService.getChatRoomsInfoByUser(logInUserId);
        // 커스텀 클래스에 담아서 반환
        ChatRoomsInfoResponse response = new ChatRoomsInfoResponse(chatRooms, chatRoomsInfoByUser);

        return ResponseEntity.ok(response);
    }

    // STOMP 메시지 핸들러 아래 주소들은 /app을 붙여서 요청하면됨
    @MessageMapping("/chatRoom/create")
    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    public void createChatRoom(@Payload CreateChatRoomRequest request, Principal principal) {
        userChatRoomService.createChatRoom(principal.getName(), request.getNicknames(), false);
    }

    @MessageMapping("/chatRoom/createDuplicate")
    @Operation(summary = "중복 채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    public void createDuplicateChatRoom(@Payload CreateChatRoomRequest request, Principal principal) {
        userChatRoomService.createChatRoom(principal.getName(), request.getNicknames(), true);
    }

    @MessageMapping("/chat/send")
    @Operation(summary = "채팅 메시지 전송", description = "지정된 채팅방에 채팅 메시지를 전송합니다.")
    public void sendMessage(@Payload CreateMessageRequest createMessageRequest, Principal principal) {
        ChatMessageResponse response = chatMessageService.saveMessage(principal.getName(), createMessageRequest.getChatRoomId(), createMessageRequest.getContent());
        // 채팅방의 모든 클라이언트에게 메시지 전송
        messagingTemplate.convertAndSend("/topic/chatroom/" + createMessageRequest.getChatRoomId(), response);
    }

    @MessageMapping("/chat/messages")
    @Operation(summary = "메시지 조회", description = "특정 채팅방의 메시지를 조회합니다.")
    public void getRecentMessages(@Payload ChatRoomIdRequest chatRoomIdRequest, Principal principal) {
        chatMessageService.getRecentMessages(chatRoomIdRequest.getChatRoomId(), principal.getName());

    }

    @MessageMapping("/chat/messages/before")
    @Operation(summary = "이전 메시지 조회", description = "특정 채팅방에서 주어진 최대 score 값보다 낮은 메시지를 조회합니다.")
    public void getMessagesBefore(@Payload ChatMessageBeforeRequest chatMessageBeforeRequest, Principal principal) {
        chatMessageService.getMessagesBefore(chatMessageBeforeRequest.getChatRoomId(), principal.getName(), chatMessageBeforeRequest.getTimestamp());
    }

    @MessageMapping("/chatRoom/invite")
    @Operation(summary = "채팅방에 유저 초대", description = "기존 채팅방에 유저를 초대합니다.")
    public void inviteUser(@Payload InviteUserRequest inviteUserRequest) {
        userChatRoomService.inviteUser(inviteUserRequest.getChatRoomId(), inviteUserRequest.getNicknames());
    }

    @MessageMapping("/chatRoom/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나갑니다.")
    public void leaveChatRoom(@Payload ChatRoomIdRequest chatRoomIdRequest, Principal principal) {
        userChatRoomService.leaveChatRoom(chatRoomIdRequest.getChatRoomId(), principal.getName());
    }

    @MessageMapping("/chatRoom/update")
    @Operation(summary = "채팅방 업데이트 정보 조회", description = "특정 채팅방의 업데이트 정보를 조회합니다.")
    public void getChatRoomUpdate(@Payload ChatRoomIdRequest chatRoomIdRequest, Principal principal) {
        chatMessageService.getChatRoomInfo(principal.getName(), chatRoomIdRequest.getChatRoomId());
    }

    @MessageMapping("/chatRoom/nameUpdate")
    @Operation(summary = "채팅방 이름 변경", description = "채팅방의 이름을 변경합니다.")
    public void updateChatRoomName(@Payload UpdateRoomNameRequest updateRoomNameRequest, Principal principal) {
        userChatRoomService.updateChatRoomName(updateRoomNameRequest.getChatRoomId(), principal.getName(), updateRoomNameRequest.getNewChatRoomName());
    }
}