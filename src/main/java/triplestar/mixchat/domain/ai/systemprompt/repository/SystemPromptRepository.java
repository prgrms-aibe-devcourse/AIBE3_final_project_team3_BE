package triplestar.mixchat.domain.ai.systemprompt.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.ai.systemprompt.constant.PromptKey;
import triplestar.mixchat.domain.ai.systemprompt.entity.SystemPrompt;

@Repository
public interface SystemPromptRepository extends JpaRepository<SystemPrompt, Long> {
    Optional<SystemPrompt> findByPromptKey(PromptKey promptKey);
}
