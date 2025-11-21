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
@Table(name = "chat_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 다형적 연관관계를 위해 chatRoomId와 conversationType 필드 추가
    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", nullable = false)
    private ChatMessage.ConversationType conversationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    private LocalDateTime lastReadAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatNotificationSetting chatNotificationSetting;

    // 생성자로 chatRoom 대신 chatRoomId와 conversationType
    public ChatMember(Member member, Long chatRoomId, ChatMessage.ConversationType conversationType, UserType userType) {
        if (member == null) {
            throw new IllegalArgumentException("member는 null일 수 없습니다.");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId는 null일 수 없습니다.");
        }
        if (conversationType == null) {
            throw new IllegalArgumentException("conversationType은 null일 수 없습니다.");
        }
        if (userType == null) {
            throw new IllegalArgumentException("userType은 null일 수 없습니다.");
        }

        this.member = member;
        this.chatRoomId = chatRoomId;
        this.conversationType = conversationType;
        this.userType = userType;
        // 기본 알림 설정은 ALWAYS로 설정
        this.chatNotificationSetting = ChatNotificationSetting.ALWAYS;
        
        //lastReadAt은 채팅 읽은 사람 수 기능 구현시 추가 예정
    }

    public enum UserType {
        ROOM_MEMBER, ROOM_OWNER
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
}
