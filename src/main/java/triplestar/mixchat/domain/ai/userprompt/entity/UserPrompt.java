package triplestar.mixchat.domain.ai.userprompt.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.ai.userprompt.constant.RolePlayType;
import triplestar.mixchat.global.jpa.entity.BaseEntity;
import triplestar.mixchat.domain.ai.userprompt.constant.UserPromptType;
import triplestar.mixchat.domain.member.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_prompts")
public class UserPrompt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = false)
    private UserPromptType type;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_play_type")
    private RolePlayType rolePlayType;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    private UserPrompt(Member member, String title, String content, UserPromptType type) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.type = type;
    }

    // 도메인 생성 메소드
    public static UserPrompt create(Member member, String title, String content, String promptType) {
        return new UserPrompt(member, title, content, UserPromptType.valueOf(promptType));
    }

    // 도메인 수정 메소드
    public void modify(String title, String content, String promptType) {
        this.title = title;
        this.content = content;
        this.type = UserPromptType.valueOf(promptType);
    }

    public boolean isDefaultPrompt() {
        return this.member == null;
    }
}