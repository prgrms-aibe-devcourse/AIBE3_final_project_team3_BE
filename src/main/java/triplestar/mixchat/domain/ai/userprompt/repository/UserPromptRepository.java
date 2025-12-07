package triplestar.mixchat.domain.ai.userprompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.ai.userprompt.constant.UserPromptType;
import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;

import java.util.List;

public interface UserPromptRepository extends JpaRepository<UserPrompt, Long> {
    List<UserPrompt> findByPromptType(UserPromptType promptType);

    @Query("SELECT p FROM UserPrompt p WHERE p.promptType = :preScriptedType OR (p.promptType = :customType AND p.member.id = :memberId)")
    List<UserPrompt> findForPremium(@Param("preScriptedType") UserPromptType preScriptedType,
                                    @Param("customType") UserPromptType customType,
                                    @Param("memberId") Long memberId);

}
