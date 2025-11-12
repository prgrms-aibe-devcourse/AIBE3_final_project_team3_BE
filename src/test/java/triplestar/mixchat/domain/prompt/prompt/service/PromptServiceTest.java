package triplestar.mixchat.domain.prompt.prompt.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptReq;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptDetailResp;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptListResp;

@SpringBootTest
@Transactional
@DisplayName("프롬프트 - 서비스")
class PromptServiceTest {

    @Autowired
    private PromptService promptService;

    @Test
    @DisplayName("프롬프트 생성")
    void createPrompt() {
        PromptReq req = new PromptReq("생성 테스트 프롬프트", "생성 내용", "CUSTOM");

        PromptDetailResp saved = promptService.create(req);

        assertThat(saved.title()).isEqualTo("생성 테스트 프롬프트");
        assertThat(saved.content()).isEqualTo("생성 내용");
        assertThat(saved.promptType()).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("프롬프트 목록 조회")
    void listPrompt() {
        promptService.create(new PromptReq("조회 테스트 프롬프트", "조회 내용", "CUSTOM"));

        java.util.List<PromptListResp> allPrompts = promptService.list();

        assertThat(allPrompts).isNotEmpty();
        assertThat(allPrompts.get(0).title()).isEqualTo("조회 테스트 프롬프트");
        assertThat(allPrompts.get(0)).isInstanceOf(PromptListResp.class);
    }

    @Test
    @DisplayName("프롬프트 상세 조회")
    void detailPrompt() {
        PromptReq req = new PromptReq("상세 프롬프트", "상세 내용", "CUSTOM");
        PromptDetailResp saved = promptService.create(req);
        Long id = saved.id();

        PromptDetailResp detail = promptService.detail(id);

        assertThat(detail.title()).isEqualTo("상세 프롬프트");
        assertThat(detail.content()).isEqualTo("상세 내용");
        assertThat(detail.promptType()).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("프롬프트 수정")
    void updatePrompt() {
        PromptReq req = new PromptReq("수정 전 프롬프트", "수정 전 내용", "CUSTOM");
        PromptDetailResp saved = promptService.create(req);
        Long id = saved.id();

        PromptReq updateReq = new PromptReq("수정 후 프롬프트", "수정 후 내용", "CUSTOM");
        PromptDetailResp updated = promptService.update(id, updateReq);

        assertThat(updated.title()).isEqualTo("수정 후 프롬프트");
        assertThat(updated.content()).isEqualTo("수정 후 내용");
        assertThat(updated.promptType()).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("프롬프트 삭제")
    void deletePrompt() {
        PromptReq req = new PromptReq("삭제 프롬프트", "삭제 내용", "CUSTOM");
        PromptDetailResp saved = promptService.create(req);
        Long id = saved.id();

        promptService.delete(id);
        var allPrompts = promptService.list();

        assertThat(allPrompts).isEmpty();
    }
}
