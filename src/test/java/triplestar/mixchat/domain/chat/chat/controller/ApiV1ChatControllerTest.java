package triplestar.mixchat.domain.chat.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;
import triplestar.mixchat.domain.ai.systemprompt.service.AiFeedbackService;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.AIChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.ChatMemberResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.DirectChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.GroupChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.JoinGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.MessagePageResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.TransferOwnerReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage.MessageType;
import triplestar.mixchat.domain.chat.chat.service.AIChatRoomService;
import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.DirectChatRoomService;
import triplestar.mixchat.domain.chat.chat.service.GroupChatRoomService;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.security.JwtAuthorizationFilter;

@ImportAutoConfiguration(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    MongoRepositoriesAutoConfiguration.class,
    MongoReactiveAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.data.mongodb.auditing.enabled=false"
})
@WebMvcTest(ApiV1ChatController.class)
@DisplayName("ApiV1ChatController 단위 테스트")
class ApiV1ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @MockitoBean
    private DirectChatRoomService directChatRoomService;

    @MockitoBean
    private GroupChatRoomService groupChatRoomService;

    @MockitoBean
    private AIChatRoomService aiChatRoomService;

    @MockitoBean
    private ChatMemberService chatMemberService;

    @MockitoBean
    private ChatMessageService chatMessageService;

    @MockitoBean
    private S3Uploader s3Uploader;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private AiFeedbackService aiFeedbackService;

    @MockitoBean
    private MongoMappingContext mongoMappingContext;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private CustomUserDetails currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new CustomUserDetails(1L, Role.ROLE_MEMBER, "tester");
    }

    @Test
    @DisplayName("AI 피드백 분석 요청 성공")
    void analyzeFeedback_Success() throws Exception {
        // given
        AiFeedbackReq req = new AiFeedbackReq("original", "translated");
        AiFeedbackResp resp = new AiFeedbackResp("corrected", Collections.emptyList());

        given(aiFeedbackService.analyze(any(AiFeedbackReq.class))).willReturn(resp);

        // when & then
        mockMvc.perform(post("/api/v1/chats/feedback")
                        .with(user(currentUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("AI 피드백 분석 성공"))
                .andExpect(jsonPath("$.data.correctedContent").value("corrected"))
                .andDo(print());
    }

    @Test
    @DisplayName("1:1 채팅방 생성 성공")
    void createDirectRoom_Success() throws Exception {
        // given
        CreateDirectChatReq req = new CreateDirectChatReq(2L);
        ChatMemberResp u1 = new ChatMemberResp(1L, "tester", false, "url1");
        ChatMemberResp u2 = new ChatMemberResp(2L, "partner", false, "url2");
        DirectChatRoomResp resp = new DirectChatRoomResp(10L, u1, u2, 0L, LocalDateTime.now(), "lastMsg");

        given(directChatRoomService.findOrCreateDirectChatRoom(anyLong(), anyLong(), anyString()))
                .willReturn(resp);

        // when & then
        mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(currentUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("1:1 채팅방 생성/조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andDo(print());
    }

    @Test
    @DisplayName("그룹 채팅방 생성 성공")
    void createGroupRoom_Success() throws Exception {
        // given
        CreateGroupChatReq req = new CreateGroupChatReq("MyGroup", List.of(2L), "password", "Desc", "Topic");
        GroupChatRoomResp resp = new GroupChatRoomResp(
                20L, "MyGroup", "Desc", "Topic", true, 5,
                LocalDateTime.now(), 1L, 0L, Collections.emptyList(),
                LocalDateTime.now(), "lastMsg"
        );

        given(groupChatRoomService.createGroupRoom(any(CreateGroupChatReq.class), anyLong()))
                .willReturn(resp);

        // when & then
        mockMvc.perform(post("/api/v1/chats/rooms/group")
                        .with(user(currentUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("그룹 채팅방 생성에 성공하였습니다."))
                .andExpect(jsonPath("$.data.name").value("MyGroup"))
                .andDo(print());
    }

    @Test
    @DisplayName("AI 채팅방 생성 성공")
    void createAiRoom_Success() throws Exception {
        // given
        CreateAIChatReq req = new CreateAIChatReq("AiRoom", 2L, AiChatRoomType.ROLE_PLAY);
        AIChatRoomResp resp = new AIChatRoomResp(30L, "AiRoom", 2L);

        given(aiChatRoomService.createAIChatRoom(anyLong(), any(CreateAIChatReq.class)))
                .willReturn(resp);

        // when & then
        mockMvc.perform(post("/api/v1/chats/rooms/ai")
                        .with(user(currentUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("AI 채팅방 생성에 성공하였습니다."))
                .andExpect(jsonPath("$.data.name").value("AiRoom"))
                .andDo(print());
    }

    @Test
    @DisplayName("1:1 채팅방 목록 조회 성공")
    void getDirectChatRooms_Success() throws Exception {
        // given
        ChatMemberResp u1 = new ChatMemberResp(1L, "tester", false, "url1");
        ChatMemberResp u2 = new ChatMemberResp(2L, "partner", false, "url2");
        List<DirectChatRoomResp> list = List.of(
                new DirectChatRoomResp(10L, u1, u2, 0L, LocalDateTime.now(), "lastMsg")
        );
        given(directChatRoomService.getRoomsForUser(anyLong())).willReturn(list);

        // when & then
        mockMvc.perform(get("/api/v1/chats/rooms/direct")
                        .with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(10L))
                .andDo(print());
    }

    @Test
    @DisplayName("참여중인 그룹 채팅방 목록 조회 성공")
    void getGroupChatRooms_Success() throws Exception {
        // given
        List<GroupChatRoomResp> list = List.of(
                new GroupChatRoomResp(
                        20L, "group1", "desc", "topic", true, 5,
                        LocalDateTime.now(), 1L, 0L, Collections.emptyList(),
                        LocalDateTime.now(), "msg"
                )
        );
        given(groupChatRoomService.getRoomsForUser(anyLong())).willReturn(list);

        // when & then
        mockMvc.perform(get("/api/v1/chats/rooms/group")
                        .with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("group1"))
                .andDo(print());
    }

    @Test
    @DisplayName("공개 그룹 채팅방 목록 조회 성공")
    void getPublicGroupChatRooms_Success() throws Exception {
        // given
        List<GroupChatRoomResp> list = List.of(
                new GroupChatRoomResp(
                        21L, "publicGroup", "desc", "topic", false, 10,
                        LocalDateTime.now(), 2L, 0L, Collections.emptyList(),
                        LocalDateTime.now(), "msg"
                )
        );
        given(groupChatRoomService.getGroupPublicRooms(anyLong())).willReturn(list);

        // when & then
        mockMvc.perform(get("/api/v1/chats/rooms/group/public")
                        .with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("publicGroup"))
                .andDo(print());
    }

    @Test
    @DisplayName("그룹 채팅방 입장 성공")
    void joinGroupRoom_Success() throws Exception {
        // given
        Long roomId = 20L;
        JoinGroupChatReq req = new JoinGroupChatReq("password");
        GroupChatRoomResp resp = new GroupChatRoomResp(
                roomId, "group1", "desc", "topic", true, 5,
                LocalDateTime.now(), 1L, 0L, Collections.emptyList(),
                LocalDateTime.now(), "msg"
        );

        given(groupChatRoomService.joinGroupRoom(anyLong(), anyLong(), anyString())).willReturn(resp);

        // when & then
        mockMvc.perform(post("/api/v1/chats/rooms/group/{roomId}/join", roomId)
                        .with(user(currentUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(roomId))
                .andDo(print());
    }

    @Test
    @DisplayName("AI 채팅방 목록 조회 성공")
    void getAiChatRooms_Success() throws Exception {
        // given
        List<AIChatRoomResp> list = List.of(
                new AIChatRoomResp(30L, "AiRoom", 2L)
        );
        given(aiChatRoomService.getRoomsForUser(anyLong())).willReturn(list);

        // when & then
        mockMvc.perform(get("/api/v1/chats/rooms/ai")
                        .with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("aiRoom"))
                .andDo(print());
    }

    @Test
    @DisplayName("메시지 목록 조회 성공")
    void getMessages_Success() throws Exception {
        // given
        Long roomId = 10L;
        MessagePageResp msgPage = new MessagePageResp(Collections.emptyList(), 0L, false);
        
        doNothing().when(chatMemberService).markAsReadOnEnter(anyLong(), anyLong(), any(ChatRoomType.class));
        given(chatMessageService.getMessagesWithSenderInfo(anyLong(), any(ChatRoomType.class), anyLong(), any(), any()))
                .willReturn(msgPage);

        // when & then
        mockMvc.perform(get("/api/v1/chats/rooms/{roomId}/messages", roomId)
                        .with(user(currentUser))
                        .param("chatRoomType", "DIRECT")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("DIRECT"))
                .andDo(print());
    }

    @Test
    @DisplayName("파일 업로드 및 메시지 전송 성공")
    void uploadFile_Success() throws Exception {
        // given
        Long roomId = 10L;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        String uploadedUrl = "https://s3.url/test.jpg";
        MessageResp msgResp = new MessageResp(
                "msgId", 1L, "tester", "https://s3.url/test.jpg", null,
                false, LocalDateTime.now(), MessageType.IMAGE, 0L, 0
        );

        given(s3Uploader.uploadFile(any(), anyString())).willReturn(uploadedUrl);
        given(chatMessageService.saveFileMessage(anyLong(), anyLong(), anyString(), anyString(), any(MessageType.class), any(ChatRoomType.class)))
                .willReturn(msgResp);

        // when & then
        mockMvc.perform(multipart("/api/v1/chats/rooms/{roomId}/files", roomId)
                        .file(file)
                        .with(user(currentUser))
                        .with(csrf())
                        .param("chatRoomType", "DIRECT")
                        .param("messageType", "IMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value(uploadedUrl))
                .andDo(print());

        verify(messagingTemplate).convertAndSend(anyString(), any(MessageResp.class));
    }

    @Test
    @DisplayName("채팅방 나가기 성공")
    void leaveRoom_Success() throws Exception {
        // given
        Long roomId = 10L;

        // when & then
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}", roomId)
                        .with(user(currentUser))
                        .with(csrf())
                        .param("chatRoomType", "DIRECT"))
                .andExpect(status().isOk())
                .andDo(print());
        
        verify(directChatRoomService).leaveRoom(roomId, currentUser.getId());
    }

    @Test
    @DisplayName("멤버 강퇴 성공")
    void kickMember_Success() throws Exception {
        // given
        Long roomId = 20L;
        Long targetId = 99L;

        doNothing().when(groupChatRoomService).kickMember(anyLong(), anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}/members/{memberId}", roomId, targetId)
                        .with(user(currentUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("멤버를 강퇴했습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("방장 위임 성공")
    void transferOwnership_Success() throws Exception {
        // given
        Long roomId = 20L;
        TransferOwnerReq req = new TransferOwnerReq(99L);

        doNothing().when(groupChatRoomService).transferOwnership(anyLong(), anyLong(), anyLong());

        // when & then
        mockMvc.perform(patch("/api/v1/chats/rooms/{roomId}/owner", roomId)
                        .with(user(currentUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("방장을 위임했습니다."))
                .andDo(print());
    }
}
