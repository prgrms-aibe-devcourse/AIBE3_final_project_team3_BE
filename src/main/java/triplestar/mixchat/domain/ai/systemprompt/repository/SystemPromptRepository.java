package triplestar.mixchat.domain.ai.systemprompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.ai.systemprompt.entity.SystemPrompt;

@Repository
public interface SystemPromptRepository extends JpaRepository<SystemPrompt, Long> {
}
