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
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.security.CustomUserDetailsService;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = ChatController.class)
@DisplayName("채팅 컨트롤러 단위 테스트")
class ChatControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockitoBean private ChatRoomService chatRoomService;
    @MockitoBean private ChatMessageService chatMessageService;
    @MockitoBean private MemberRepository memberRepository;
    @MockitoBean private S3Uploader s3Uploader;
    @MockitoBean private SimpMessagingTemplate messagingTemplate;
    @MockitoBean private AuthJwtProvider authJwtProvider;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Member mockUser;
    private CustomUserDetails mockUserDetails;

    private Member createMockMember(Long id, String email, String nickname) {
        Member member = Member.createMember(
                email,
                Password.encrypt("ValidPassword123", passwordEncoder),
                nickname,
                nickname,
                Country.SOUTH_KOREA,
                EnglishLevel.BEGINNER,
                List.of("테스트"),
                "테스트 설명"
        );
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

        ChatRoom mockRoom = new ChatRoom();
        ReflectionTestUtils.setField(mockRoom, "id", 100L);
        mockRoom.setName("테스트유저, 파트너유저");
        mockRoom.setRoomType(ChatRoom.RoomType.DIRECT);

        given(memberRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
        given(chatRoomService.findOrCreateDirectRoom(any(Member.class), any(Long.class))).willReturn(mockRoom);

        // when
        mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(mockUserDetails))
                        .with(csrf()) // CSRF 토큰 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("1:1 채팅방 생성/조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.name").value("테스트유저, 파트너유저"));

        verify(chatRoomService).findOrCreateDirectRoom(any(Member.class), any(Long.class));
    }

    @Test
    @DisplayName("자신의 모든 채팅방 목록 조회 성공")
    void getRooms_success() throws Exception {
        // given
        ChatRoom mockRoom = new ChatRoom();
        ReflectionTestUtils.setField(mockRoom, "id", 100L);
        mockRoom.setName("테스트 채팅방");
        mockRoom.setRoomType(ChatRoom.RoomType.GROUP);

        Member memberInRoom = createMockMember(2L, "userInRoom@example.com", "참여자");

        ChatMember chatMember = new ChatMember();
        chatMember.setMember(memberInRoom);
        mockRoom.setMembers(List.of(chatMember));

        given(memberRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
        given(chatRoomService.getRoomsForUser(any(Member.class))).willReturn(Collections.singletonList(mockRoom));

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

        verify(chatRoomService).getRoomsForUser(any(Member.class));
    }

    @Test
    @DisplayName("채팅방 나가기 성공")
    void leaveRoom_success() throws Exception {
        // given
        long roomId = 100L;
        given(memberRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));

        // when
        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}/leave", roomId)
                        .with(user(mockUserDetails))
                        .with(csrf())) // CSRF 토큰 추가
                .andDo(print())

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("채팅방 나가기에 성공하였습니다."));

        verify(chatRoomService).leaveRoom(roomId, mockUser);
    }
}
