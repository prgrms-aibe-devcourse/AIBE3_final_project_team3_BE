package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class    ChatMember extends BaseEntity {

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
        if (member == null) {
            throw new IllegalArgumentException("member는 null일 수 없습니다.");
        }
        if (chatRoom == null) {
            throw new IllegalArgumentException("chatRoom은 null일 수 없습니다.");
        }
        if (userType == null) {
            throw new IllegalArgumentException("userType은 null일 수 없습니다.");
        }

        this.member = member;
        this.chatRoom = chatRoom;
        this.userType = userType;
        
        //lastReadAt은 채팅 읽은 사람 수 기능 구현시 추가 예정
    }

    public enum UserType {
        ROOM_MEMBER, ROOM_OWNER
    }
}
