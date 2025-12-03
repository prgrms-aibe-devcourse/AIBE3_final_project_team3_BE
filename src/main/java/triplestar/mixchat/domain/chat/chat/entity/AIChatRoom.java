package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;
import triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Getter
@Table(name = "ai_chat_rooms") // 독립적인 테이블 사용
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AIChatRoom extends BaseEntity {

    @Column(nullable = false)
    private String name; // 채팅방 이름

    // AI 채팅방에만 필요한 필드 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private UserPrompt persona; // AI 페르소나 (성격, 역할 등)

    @Column(nullable = false)
    private Long currentSequence = 0L; // 채팅방 메시지 순서 번호

    @Column(nullable = false)
    private AiChatRoomType roomType;

    private AIChatRoom(String name, UserPrompt persona, AiChatRoomType roomType) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채팅방 이름(name)은 비어 있을 수 없습니다.");
        }
        if (persona == null) {
            throw new IllegalArgumentException("페르소나는 null일 수 없습니다.");
        }
        if (roomType == null) {
            throw new IllegalArgumentException("채팅방 타입(roomType)은 null일 수 없습니다.");
        }
        this.name = name;
        this.persona = persona;
        this.roomType = roomType;
    }

    // AI 채팅방 생성용 정적 팩토리 메서드

    public static AIChatRoom create(String name, UserPrompt persona, AiChatRoomType roomType) {
        return new AIChatRoom(name, persona, roomType);
    }

    public Long generateNextSequence() {
        return ++this.currentSequence;
    }
}
