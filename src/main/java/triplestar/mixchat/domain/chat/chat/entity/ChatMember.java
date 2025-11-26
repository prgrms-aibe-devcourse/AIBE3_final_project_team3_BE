package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.chat.chat.constant.ChatNotificationSetting;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_chat_member",
        columnNames = {"member_id", "chat_room_id", "chat_room_type"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 다형적 연관관계를 위해 chatRoomId와 chatRoomType 필드 추가
    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_room_type", nullable = false)
    private ChatMessage.chatRoomType chatRoomType;

    private LocalDateTime lastReadAt;

    private Long lastReadSequence; // 마지막으로 읽은 메시지의 sequence

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatNotificationSetting chatNotificationSetting;

    // 생성자로 chatRoom 대신 chatRoomId와 chatRoomType
    public ChatMember(Member member, Long chatRoomId, ChatMessage.chatRoomType chatRoomType) {
        if (member == null) {
            throw new IllegalArgumentException("member는 null일 수 없습니다.");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId는 null일 수 없습니다.");
        }
        if (chatRoomType == null) {
            throw new IllegalArgumentException("chatRoomType은 null일 수 없습니다.");
        }

        this.member = member;
        this.chatRoomId = chatRoomId;
        this.chatRoomType = chatRoomType;
        // 기본 알림 설정은 ALWAYS로 설정
        this.chatNotificationSetting = ChatNotificationSetting.ALWAYS;

        //lastReadAt은 채팅 읽은 사람 수 기능 구현시 추가 예정
    }

    public boolean isNotificationAlways() {
        return this.chatNotificationSetting == ChatNotificationSetting.ALWAYS;
    }

    public void turnOffNotifications() {
        this.chatNotificationSetting = ChatNotificationSetting.NONE;
    }

    public void turnOnNotifications() {
        this.chatNotificationSetting = ChatNotificationSetting.ALWAYS;
    }

    // 읽은 메시지 sequence 업데이트 (뒤로 가지 않기 가드, 데이터 정합성 보장)
    public void updateLastReadSequence(Long sequence) {
        if (sequence == null) {
            return;
        }

        // 현재 값보다 큰 경우에만 업데이트
        if (this.lastReadSequence != null && sequence <= this.lastReadSequence) {
            return;
        }

        this.lastReadSequence = sequence;
        this.lastReadAt = LocalDateTime.now();
    }
}
