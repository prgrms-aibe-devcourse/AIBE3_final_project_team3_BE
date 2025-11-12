package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Column(nullable = false)
    private String name; //채팅방 이름

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChatMember> members = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    // 1:1, GROUP
    public enum RoomType {
        DIRECT, GROUP
    }

    private ChatRoom(String name, RoomType roomType) {
        this.name = name;
        this.roomType = roomType;
    }

    public static ChatRoom createGroupRoom(String name) {
        return new ChatRoom(name, RoomType.GROUP);
    }

    public static ChatRoom createDirectRoom(String member1Name, String member2Name) {
        return new ChatRoom(member1Name + ", " + member2Name, RoomType.DIRECT);
    }
}
