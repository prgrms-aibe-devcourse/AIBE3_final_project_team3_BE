package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Getter
@Table(name = "group_chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChatRoom extends BaseEntity {

    @Column(nullable = false)
    private String name; // 채팅방 이름

    private String password; // 채팅방 비밀번호 (선택)

    @Column(length = 500)
    private String description; // 방 설명
    @Column(length = 50)
    private String topic; // 방 주제

    private GroupChatRoom(String name, String description, String topic, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채팅방 이름(name)은 비어 있을 수 없습니다.");
        }
        this.name = name;
        this.description = description;
        this.topic = topic;
        this.password = password;
    }

    // 그룹 채팅방 생성용 정적 팩토리 메서드
    public static GroupChatRoom create(String name, String description, String topic, String password) {
        return new GroupChatRoom(name, description, topic, password);
    }
}
