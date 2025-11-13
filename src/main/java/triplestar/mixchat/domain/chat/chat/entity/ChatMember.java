package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "chat_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    private LocalDateTime lastReadAt;

    public ChatMember(Member member, ChatRoom chatRoom, UserType userType) {
        this.member = Objects.requireNonNull(member, "Member는 null일 수 없습니다.");
        this.chatRoom = Objects.requireNonNull(chatRoom, "ChatRoom은 null일 수 없습니다.");
        this.userType = Objects.requireNonNull(userType, "UserType은 null일 수 없습니다.");
    }

    public enum UserType {
        ROOM_MEMBER, ROOM_OWNER
    }
}
