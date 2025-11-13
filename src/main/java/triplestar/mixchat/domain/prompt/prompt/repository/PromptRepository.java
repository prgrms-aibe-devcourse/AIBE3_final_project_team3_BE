package triplestar.mixchat.domain.prompt.prompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triplestar.mixchat.domain.prompt.prompt.constant.PromptType;
import triplestar.mixchat.domain.prompt.prompt.entity.Prompt;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    List<Prompt> findByType(PromptType type);
    List<Prompt> findByTypeAndMember_Id(PromptType type, Long memberId);
}
