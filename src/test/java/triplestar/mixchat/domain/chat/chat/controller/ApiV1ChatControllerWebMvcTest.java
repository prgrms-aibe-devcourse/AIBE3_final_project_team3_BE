//package triplestar.mixchat.domain.chat.chat.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyBoolean;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.time.LocalDateTime;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
//import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
//import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
//import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
//import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
//import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;
//import triplestar.mixchat.domain.ai.systemprompt.service.AiFeedbackService;
//import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
//import triplestar.mixchat.domain.chat.chat.dto.ChatMemberResp;
//import triplestar.mixchat.domain.chat.chat.dto.DirectChatRoomResp;
//import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
//import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
//import triplestar.mixchat.domain.chat.chat.service.AIChatRoomService;
//import triplestar.mixchat.domain.chat.chat.service.ChatMemberService;
//import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
//import triplestar.mixchat.domain.chat.chat.service.DirectChatRoomService;
//import triplestar.mixchat.domain.chat.chat.service.GroupChatRoomService;
//import triplestar.mixchat.domain.member.member.constant.Role;
//import triplestar.mixchat.global.security.CustomUserDetails;
//import triplestar.mixchat.global.security.JwtAuthorizationFilter;
//import triplestar.mixchat.global.security.SecurityConfig;
//import triplestar.mixchat.global.s3.S3Uploader;
//
//@WebMvcTest(controllers = ApiV1ChatController.class)
//@ImportAutoConfiguration(exclude = {
//        MongoAutoConfiguration.class,
//        MongoDataAutoConfiguration.class,
//        MongoRepositoriesAutoConfiguration.class,
//        MongoReactiveAutoConfiguration.class
//})
//@Import(SecurityConfig.class)
//class ApiV1ChatControllerWebMvcTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private DirectChatRoomService directChatRoomService;
//
//    @MockitoBean
//    private GroupChatRoomService groupChatRoomService;
//
//    @MockitoBean
//    private AIChatRoomService aiChatRoomService;
//
//    @MockitoBean
//    private ChatMemberService chatMemberService;
//
//    @MockitoBean
//    private ChatMessageService chatMessageService;
//
//    @MockitoBean
//    private S3Uploader s3Uploader;
//
//    @MockitoBean
//    private SimpMessagingTemplate messagingTemplate;
//
//    @MockitoBean
//    private AiFeedbackService aiFeedbackService;
//
//    // Security filter bean mocked to satisfy SecurityConfig dependency
//    @MockitoBean
//    private JwtAuthorizationFilter jwtAuthorizationFilter;
//
//    // Mock JPA metamodel to avoid "JPA metamodel must not be empty" in WebMvcTest slice
//    @MockitoBean
//    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
//
//    // Mock Mongo mapping context to avoid Mongo auditing/mapping initialization in WebMvc slice
//    @MockitoBean
//    private MongoMappingContext mongoMappingContext;
//
//    private CustomUserDetails userPrincipal() {
//        return new CustomUserDetails(1L, Role.ROLE_MEMBER, "tester");
//    }
//
//    @Test
//    @DisplayName("1:1 채팅방 생성 성공 시 200 반환 및 room id 포함")
//    @WithMockUser
//    void createDirectRoom_success() throws Exception {
//        DirectChatRoomResp resp = new DirectChatRoomResp(
//                10L,
//                new ChatMemberResp(1L, "tester", false),
//                new ChatMemberResp(2L, "partner", false),
//                0L,
//                LocalDateTime.now()
//        );
//        given(directChatRoomService.findOrCreateDirectChatRoom(anyLong(), anyLong(), any())).willReturn(resp);
//
//        String body = """
//            { "partnerId": 2 }
//            """;
//
//        mockMvc.perform(post("/api/v1/chats/rooms/direct")
//                        .with(user(userPrincipal()))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.id").value(10))
//                .andExpect(jsonPath("$.data.user1.nickname").value("tester"))
//                .andExpect(jsonPath("$.data.user2.nickname").value("partner"));
//    }
//
//    @Test
//    @DisplayName("메시지 전송 성공 시 200 반환 및 내용 포함")
//    void sendMessage_success() throws Exception {
//        MessageResp messageResp = new MessageResp(
//                "msg-1",
//                1L,
//                "tester",
//                "hello",
//                null,
//                false,
//                LocalDateTime.now(),
//                ChatMessage.MessageType.TEXT,
//                1L,
//                0
//        );
//        given(chatMessageService.saveMessage(anyLong(), anyLong(), any(), any(), eq(ChatMessage.MessageType.TEXT), eq(ChatRoomType.DIRECT), anyBoolean()))
//                .willReturn(messageResp);
//
//        String body = """
//            { "content": "hello", "isTranslateEnabled": false }
//            """;
//
//        mockMvc.perform(post("/api/v1/chats/rooms/{roomId}/message", 99L)
//                        .with(user(userPrincipal()))
//                        .param("chatRoomType", "DIRECT")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.content").value("hello"))
//                .andExpect(jsonPath("$.data.messageType").value("TEXT"))
//                .andExpect(jsonPath("$.data.sequence").value(1));
//    }
//}
