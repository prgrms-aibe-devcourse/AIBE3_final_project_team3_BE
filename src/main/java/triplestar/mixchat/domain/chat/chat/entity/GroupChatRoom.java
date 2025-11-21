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

    // 그룹 채팅방에만 필요한 필드 추가
    @Column(length = 500)
    private String description; // 방 설명
    @Column(length = 50)
    private String topic; // 방 주제

    // ChatMember와의 관계는 ChatMember에서 다형적으로 관리하거나, GroupChatRoomMember와 같은 별도 엔티티로 관리.
    // 추후 ChatMember의 다형적 연관관계 설정 시 수정 필요.
    // @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // private List<ChatMember> members = new ArrayList<>();


    private GroupChatRoom(String name, String description, String topic) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채팅방 이름(name)은 비어 있을 수 없습니다.");
        }
        this.name = name;
        this.description = description;
        this.topic = topic;
    }

    // 그룹 채팅방 생성용 정적 팩토리 메서드
    public static GroupChatRoom create(String name, String description, String topic) {
        return new GroupChatRoom(name, description, topic);
    }
}
