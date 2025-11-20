package triplestar.mixchat.domain.chat.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import triplestar.mixchat.domain.chat.chat.dto.ChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

@WebMvcTest(controllers = ApiV1ChatController.class,
    excludeAutoConfiguration = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@DisplayName("채팅 컨트롤러 단위 테스트")
class ApiV1ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatRoomService chatRoomService;
    @MockBean
    private ChatMessageService chatMessageService;
    @MockBean
    private S3Uploader s3Uploader;
    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    // SecurityConfig 로딩에 필요하므로 MockBean으로 등록
    @MockBean
    private AuthJwtProvider authJwtProvider;
    // JPA Auditing 기능 로딩에 필요하므로 MockBean으로 등록
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 인증된 사용자 정보를 설정
        mockUserDetails = new CustomUserDetails(1L, Role.ROLE_MEMBER);
    }

    @Test
    @DisplayName("1:1 채팅방 생성/조회 성공")
    void createDirectRoom_success() throws Exception {
        // given
        long currentUserId = 1L;
        long partnerId = 2L;
        CreateDirectChatReq requestDto = new CreateDirectChatReq(partnerId);

        // chatRoomService.findOrCreateDirectRoom이 호출될 때 반환할 가짜 응답 객체
        ChatRoomResp mockRoomResp = new ChatRoomResp(100L, "유저1, 유저2", ChatRoom.RoomType.DIRECT, Collections.emptyList());
        given(chatRoomService.findOrCreateDirectRoom(currentUserId, partnerId)).willReturn(mockRoomResp);

        // when & then
        mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(mockUserDetails)) // Spring Security Test: 'mockUserDetails' 사용자로 인증
                        .with(csrf()) // CSRF 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("1:1 채팅방 생성/조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.name").value("유저1, 유저2"));

        // verify: chatRoomService의 메소드가 정확한 인자와 함께 호출되었는지 검증
        verify(chatRoomService).findOrCreateDirectRoom(currentUserId, partnerId);
    }

    @Test
    @DisplayName("자신의 모든 채팅방 목록 조회 성공")
    void getRooms_success() throws Exception {
        // given
        long currentUserId = 1L;
        ChatRoomResp mockRoomResp = new ChatRoomResp(100L, "테스트 채팅방", ChatRoom.RoomType.GROUP, Collections.emptyList());
        given(chatRoomService.getRoomsForUser(currentUserId)).willReturn(Collections.singletonList(mockRoomResp));

        // when & then
        mockMvc.perform(get("/api/v1/chats/rooms")
                        .with(user(mockUserDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("채팅방 목록 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(100L));

        verify(chatRoomService).getRoomsForUser(currentUserId);
    }

    @Test
    @DisplayName("채팅방 나가기 성공")
    void leaveRoom_success() throws Exception {
        // given
        long currentUserId = 1L;
        long roomId = 100L;
        // void를 반환하는 메소드는 willDoNothing()으로 설정
        willDoNothing().given(chatRoomService).leaveRoom(roomId, currentUserId);

        // when & then
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}/leave", roomId)
                        .with(user(mockUserDetails))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(chatRoomService).leaveRoom(roomId, currentUserId);
    }
}
