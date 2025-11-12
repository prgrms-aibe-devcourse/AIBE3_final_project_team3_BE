package triplestar.mixchat.domain.prompt.prompt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptReq;
import triplestar.mixchat.domain.prompt.prompt.service.PromptService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("프롬프트 - 컨트롤러")
class ApiV1PromptControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private PromptService promptService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "유저1", roles = "USER")
    @DisplayName("프롬프트 생성")
    void createPrompt() throws Exception {
        PromptReq req = new PromptReq("테스트 프롬프트", "프롬프트 내용입니다.", "CUSTOM");
        String json = objectMapper.writeValueAsString(req);

        mvc.perform(post("/api/v1/prompt/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 프롬프트"));
    }

    @Test
    @WithMockUser(username = "유저1", roles = "USER")
    @DisplayName("프롬프트 목록 조회")
    void listPrompt() throws Exception {
        promptService.create(new PromptReq("목록 프롬프트", "내용", "CUSTOM"));

        mvc.perform(get("/api/v1/prompt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("목록 프롬프트"));
    }

    @Test
    @WithMockUser(username = "유저1", roles = "USER")
    @DisplayName("프롬프트 상세 조회")
    void detailPrompt() throws Exception {
        var saved = promptService.create(new PromptReq("상세 프롬프트", "상세 내용", "CUSTOM"));
        Long id = saved.id();

        mvc.perform(get("/api/v1/prompt/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("상세 프롬프트"))
                .andExpect(jsonPath("$.content").value("상세 내용"));
    }

    @Test
    @WithMockUser(username = "유저1", roles = "USER")
    @DisplayName("프롬프트 수정")
    void modifyPrompt() throws Exception {
        var saved = promptService.create(new PromptReq("수정 프롬프트", "내용", "CUSTOM"));
        Long id = saved.id();
        PromptReq req = new PromptReq("수정된 프롬프트", "수정된 내용입니다.", "CUSTOM");
        String json = objectMapper.writeValueAsString(req);

        mvc.perform(put("/api/v1/prompt/update/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 프롬프트"));
    }

    @Test
    @WithMockUser(username = "유저1", roles = "USER")
    @DisplayName("프롬프트 삭제")
    void deletePrompt() throws Exception {
        var saved = promptService.create(new PromptReq("삭제 프롬프트", "내용", "CUSTOM"));
        Long id = saved.id();

        mvc.perform(delete("/api/v1/prompt/delete/" + id))
                .andExpect(status().isNoContent());
    }
}
