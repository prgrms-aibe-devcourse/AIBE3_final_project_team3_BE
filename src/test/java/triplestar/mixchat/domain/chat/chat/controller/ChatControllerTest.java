package triplestar.mixchat.domain.chat.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import triplestar.mixchat.domain.chat.chat.dto.ChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.security.CustomUserDetailsService;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApiV1ChatController.class)
@DisplayName("채팅 컨트롤러 단위 테스트")
class ChatControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockitoBean private ChatRoomService chatRoomService;
    @MockitoBean private ChatMessageService chatMessageService;
    @MockitoBean private S3Uploader s3Uploader;
    @MockitoBean private SimpMessagingTemplate messagingTemplate;
    @MockitoBean private AuthJwtProvider authJwtProvider;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Member mockUser;
    private CustomUserDetails mockUserDetails;

    private Member createMockMember(Long id, String email, String nickname) {
        Member member = Member.builder()
                .email(email)
                .password(Password.encrypt("ValidPassword123", passwordEncoder))
                .name(nickname)
                .nickname(nickname)
                .country(Country.SOUTH_KOREA)
                .englishLevel(EnglishLevel.BEGINNER)
                .interest("테스트")
                .description("테스트 설명")
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    @BeforeEach
    void setUp() {
        mockUser = createMockMember(1L, "user@example.com", "테스트유저");
        mockUserDetails = new CustomUserDetails(mockUser.getId(), mockUser.getRole());
    }

    @Test
    @DisplayName("1:1 채팅방 생성/조회 성공")
    void createDirectRoom_success() throws Exception {
        // given
        long partnerId = 2L;
        CreateDirectChatReq requestDto = new CreateDirectChatReq(partnerId);

        ChatRoomResp mockRoomResp = new ChatRoomResp(100L, "테스트유저, 파트너유저", ChatRoom.RoomType.DIRECT, Collections.emptyList());

        given(chatRoomService.findOrCreateDirectRoom(mockUser.getId(), partnerId)).willReturn(mockRoomResp);

        // when
        mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(mockUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("1:1 채팅방 생성/조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.name").value("테스트유저, 파트너유저"));

        verify(chatRoomService).findOrCreateDirectRoom(mockUser.getId(), partnerId);
    }

    @Test
    @DisplayName("자신의 모든 채팅방 목록 조회 성공")
    void getRooms_success() throws Exception {
        // given
        ChatRoomResp mockRoomResp = new ChatRoomResp(100L, "테스트 채팅방", ChatRoom.RoomType.GROUP, Collections.emptyList());

        given(chatRoomService.getRoomsForUser(mockUser.getId())).willReturn(Collections.singletonList(mockRoomResp));

        // when
        mockMvc.perform(get("/api/v1/chats/rooms")
                        .with(user(mockUserDetails)))
                .andDo(print())

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("채팅방 목록 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(100L))
                .andExpect(jsonPath("$.data[0].name").value("테스트 채팅방"));

        verify(chatRoomService).getRoomsForUser(mockUser.getId());
    }

    @Test
    @DisplayName("채팅방 나가기 성공")
    void leaveRoom_success() throws Exception {
        // given
        long roomId = 100L;
        willDoNothing().given(chatRoomService).leaveRoom(anyLong(), anyLong());

        // when
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}/leave", roomId)
                        .with(user(mockUserDetails))
                        .with(csrf()))
                .andDo(print())

                // then
                .andExpect(status().isOk());

        verify(chatRoomService).leaveRoom(roomId, mockUser.getId());
    }
}
