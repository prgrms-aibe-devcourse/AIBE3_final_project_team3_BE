package triplestar.mixchat.domain.ai.systemprompt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.ai.systemprompt.constant.PromptKey;
import triplestar.mixchat.domain.ai.systemprompt.entity.SystemPrompt;
import triplestar.mixchat.domain.ai.systemprompt.repository.SystemPromptRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemPromptService {

    private final SystemPromptRepository systemPromptRepository;

    public SystemPrompt getLatestByKey(String promptKey) {
        return systemPromptRepository.findTopByPromptKeyOrderByVersionDesc(promptKey)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 이름의 프롬프트가 존재하지 않습니다. promptKey=" + promptKey));
    }

    public SystemPrompt getLatestByKey(PromptKey promptKey) {
        return getLatestByKey(promptKey.key());
    }
}
