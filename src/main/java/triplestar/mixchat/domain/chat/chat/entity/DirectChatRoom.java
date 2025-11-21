package triplestar.mixchat.domain.chat.chat.entity; // Package updated

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntity;
@Entity
@Getter
@Table(name = "direct_chat_rooms",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user1_id", "user2_id"}
       ))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectChatRoom extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private Member user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private Member user2;

    private DirectChatRoom(Member user1, Member user2) {
        if (user1 == null || user2 == null) {
            throw new IllegalArgumentException("두 참여자는 null일 수 없습니다.");
        }
        if (user1.getId().equals(user2.getId())) {
            throw new IllegalArgumentException("1:1 채팅 참여자는 서로 달라야 합니다.");
        }

        // user1_id < user2_id 순으로 항상 정렬하여 중복 방 생성 방지
        if (user1.getId() < user2.getId()) {
            this.user1 = user1;
            this.user2 = user2;
        } else {
            this.user1 = user2;
            this.user2 = user1;
        }
    }

    //1:1 채팅방 생성용 정적 팩토리 메서드
    public static DirectChatRoom create(Member user1, Member user2) {
        return new DirectChatRoom(user1, user2);
    }
}
