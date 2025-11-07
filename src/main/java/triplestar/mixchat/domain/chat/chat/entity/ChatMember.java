package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import triplestar.mixchat.global.jpa.entity.BaseEntity;
import triplestar.mixchat.domain.member.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private LocalDateTime lastReadAt;

    public enum UserType {
        ROOM_MEMBER, ROOM_OWNER
    }
}
