package triplestar.mixchat.domain.prompt.prompt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.global.jpa.entity.BaseEntity;
import triplestar.mixchat.domain.prompt.prompt.constant.PromptType;
import triplestar.mixchat.domain.member.member.entity.Member;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "prompts")
public class Prompt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = false)
    private PromptType type;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;
}
