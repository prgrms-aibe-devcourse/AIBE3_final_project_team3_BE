package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; //채팅방 이름

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChatMember> members = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RoomType roomType; // 1:1 / GROUP

    public enum RoomType {
        DIRECT, GROUP
    }
}