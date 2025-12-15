package triplestar.mixchat.domain.chat.chat.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;

import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackReq;

import triplestar.mixchat.domain.ai.systemprompt.dto.AiFeedbackResp;

import triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType;

import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;

import triplestar.mixchat.domain.member.member.entity.Member;

import triplestar.mixchat.global.security.CustomUserDetails;

import triplestar.mixchat.testutils.BaseChatIntegrationTest;



class ApiV1AIChatControllerIntegrationTest extends BaseChatIntegrationTest {



    private Member user1;

    private CustomUserDetails user1Details;

    private UserPrompt userPrompt;



    @BeforeEach

    void setUp() {

        aiChatRoomRepository.deleteAll();

        // BaseChatIntegrationTest.baseSetUp()이 이미 personaRepository.deleteAll()을 호출함

        

        user1 = createMember("user1");

        user1Details = toUserDetails(user1);

        

        // UserPrompt 생성 및 저장 (CUSTOM 타입)

        userPrompt = userPromptRepository.save(

            UserPrompt.create(user1, "My AI", "You are a helpful assistant.", "CUSTOM")

        );

    }



    @Test

    @DisplayName("AI 채팅방 생성 -> 조회 -> 나가기")

    void aiChat_lifecycle() throws Exception {

        // 1. 생성 (저장된 UserPrompt ID 사용)

        CreateAIChatReq createReq = new CreateAIChatReq("AI Helper", userPrompt.getId(), AiChatRoomType.ROLE_PLAY);

        String response = mockMvc.perform(post("/api/v1/chats/rooms/ai")

                        .with(user(user1Details))

                        .with(csrf())

                        .contentType(MediaType.APPLICATION_JSON)

                        .content(objectMapper.writeValueAsString(createReq)))

                .andExpect(status().isOk())

                .andReturn()

                .getResponse()

                .getContentAsString();



        Long roomId = objectMapper.readTree(response).path("data").path("id").asLong();



        // 2. 조회

        mockMvc.perform(get("/api/v1/chats/rooms/ai")

                        .with(user(user1Details)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data[0].id").value(roomId));



        // 3. 나가기

        mockMvc.perform(delete("/api/v1/chats/rooms/{roomId}", roomId)

                        .with(user(user1Details))

                        .with(csrf())

                        .param("chatRoomType", "AI"))

                .andExpect(status().isOk());

    }



    @Test

    @DisplayName("AI 피드백 요청 테스트")

    void aiFeedback_analyze() throws Exception {

        // AI 피드백은 채팅방 생성 여부와 관계없이 독립적으로 동작할 수도 있지만,

        // 보통 특정 상황극이 끝난 후 요청하므로 가상의 데이터를 보낸다.

        AiFeedbackReq req = new AiFeedbackReq("everyone 반갑습니다!", "Nice to meet you, everyone!");



        // Mocking된 서비스 동작 정의

        AiFeedbackResp mockResp = new AiFeedbackResp(

            "Nice to meet you, everyone!",

            List.of()

        );

        when(aiFeedbackService.analyze(any())).thenReturn(mockResp);



        mockMvc.perform(post("/api/v1/chats/feedback")

                        .with(user(user1Details))

                        .with(csrf())

                        .contentType(MediaType.APPLICATION_JSON)

                        .content(objectMapper.writeValueAsString(req)))

                .andExpect(status().isOk()) 

                .andExpect(jsonPath("$.msg").exists())

                .andExpect(jsonPath("$.data.correctedContent").value("Nice to meet you, everyone!"));

    }

}


