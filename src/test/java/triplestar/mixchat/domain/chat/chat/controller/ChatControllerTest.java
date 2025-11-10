package triplestar.mixchat.domain.chat.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import triplestar.mixchat.domain.chat.chat.dto.CreateDirectChatReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.ChatRoomService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.s3.S3Uploader;
import triplestar.mixchat.global.security.CustomUserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@DisplayName("채팅 컨트롤러 단위 테스트")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ChatController가 의존하는 빈들을 MockBean으로 등록
    @MockitoBean
    private ChatRoomService chatRoomService;
    @MockBean
    private ChatMessageService chatMessageService;
    @MockBean
    private MemberRepository memberRepository;
    @MockBean
    private S3Uploader s3Uploader;
    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private Member mockUser;
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 기본 사용자 객체 설정
        mockUser = Member.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("테스트유저")
                .build();
        
        mockUserDetails = new CustomUserDetails(mockUser);
    }

    @Test
    @DisplayName("1:1 채팅방 생성/조회 성공")
    void createDirectRoom_success() throws Exception {
        // given (준비)
        long partnerId = 2L;
        CreateDirectChatReq requestDto = new CreateDirectChatReq(partnerId);

        // 서비스가 반환할 가짜 ChatRoom 객체 생성
        ChatRoom mockRoom = new ChatRoom();
        mockRoom.setId(100L);
        mockRoom.setName("테스트유저, 파트너유저");
        mockRoom.setRoomType(ChatRoom.RoomType.DIRECT);

        // 컨트롤러의 getCurrentMember가 memberRepository를 호출할 때, mockUser를 반환하도록 설정
        given(memberRepository.findById(mockUser.getId())).willReturn(Optional.of(mockUser));
        // chatRoomService가 호출될 때, 위에서 만든 가짜 채팅방을 반환하도록 설정
        given(chatRoomService.findOrCreateDirectRoom(any(Member.class), any(Long.class))).willReturn(mockRoom);

        // when (실행)
        mockMvc.perform(post("/api/v1/chats/rooms/direct")
                        .with(user(mockUserDetails)) // 인증된 사용자 정보 전달
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())

        // then (검증)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("1:1 채팅방 생성/조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.name").value("테스트유저, 파트너유저"));

        // chatRoomService의 findOrCreateDirectRoom 메서드가 정확히 1번 호출되었는지 검증
        verify(chatRoomService).findOrCreateDirectRoom(any(Member.class), any(Long.class));
    }

    @Test
    @DisplayName("자신의 모든 채팅방 목록 조회 성공")
    void getRooms_success() throws Exception {
        // given
        ChatRoom mockRoom = new ChatRoom();
        mockRoom.setId(100L);
        mockRoom.setName("테스트 채팅방");
        mockRoom.setRoomType(ChatRoom.RoomType.GROUP);
        
        Member memberInRoom = Member.builder().id(1L).nickname("테스트유저").build();
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
                .andExpect(jsonPath("$.success").value(true))
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
                        .with(user(mockUserDetails)))
                .andDo(print())

        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("채팅방 나가기에 성공하였습니다."));

        verify(chatRoomService).leaveRoom(roomId, mockUser);
    }
}
