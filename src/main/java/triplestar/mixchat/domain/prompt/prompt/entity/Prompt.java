package triplestar.mixchat.domain.prompt.prompt.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.constant.MembershipGrade;
import triplestar.mixchat.global.jpa.entity.BaseEntity;
import triplestar.mixchat.domain.prompt.prompt.constant.PromptType;
import triplestar.mixchat.domain.member.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "prompts")
public class Prompt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = false)
    private PromptType type;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    private Prompt(Member member, String title, String content, PromptType type) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.type = type;
    }

    // 도메인 생성 메소드
    public static Prompt create(Member member, String title, String content, String promptType) {
        return new Prompt(member, title, content, PromptType.valueOf(promptType));
    }

    // 도메인 수정 메소드
    public void modify(String title, String content, String promptType) {
        this.title = title;
        this.content = content;
        this.type = PromptType.valueOf(promptType);
    }

    public boolean isDefaultPrompt() {
        return this.member == null;
    }
}