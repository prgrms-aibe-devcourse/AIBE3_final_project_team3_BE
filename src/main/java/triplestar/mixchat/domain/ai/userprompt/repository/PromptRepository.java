package triplestar.mixchat.domain.ai.userprompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.ai.userprompt.constant.PromptType;
import triplestar.mixchat.domain.ai.userprompt.entity.Prompt;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    List<Prompt> findByType(PromptType type);

    @Query("SELECT p FROM Prompt p WHERE p.type = :preScriptedType OR (p.type = :customType AND p.member.id = :memberId)")
    List<Prompt> findForPremium(@Param("preScriptedType") PromptType preScriptedType,
                                @Param("customType") PromptType customType,
                                @Param("memberId") Long memberId);

}
