package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Getter
@Table(name = "ai_chat_rooms") // 독립적인 테이블 사용
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AIChatRoom extends BaseEntity {

    @Column(nullable = false)
    private String name; // 채팅방 이름

    // AI 채팅방에만 필요한 필드 추가
    @Column(nullable = false)
    private String aiPersona; // AI 페르소나 (성격, 역할 등)
    @Column(nullable = false)
    private Long currentSequence = 0L; // 채팅방 메시지 순서 번호

    private AIChatRoom(String name, String aiPersona) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채팅방 이름(name)은 비어 있을 수 없습니다.");
        }
        if (aiPersona == null || aiPersona.isBlank()) {
            throw new IllegalArgumentException("AI 페르소나는 비어 있을 수 없습니다.");
        }
        this.name = name;
        this.aiPersona = aiPersona;
    }

    // AI 채팅방 생성용 정적 팩토리 메서드

    public static AIChatRoom create(String name, String aiPersona) {
        return new AIChatRoom(name, aiPersona);
    }

    public Long generateNextSequence() {
        return ++this.currentSequence;
    }
}
