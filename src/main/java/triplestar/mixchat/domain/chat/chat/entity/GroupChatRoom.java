package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Getter
@Table(name = "group_chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChatRoom extends BaseEntity {

    @Column(nullable = false)
    private String name; // 채팅방 이름

    // todo: 암호화
    private String password; // 채팅방 비밀번호 (선택)

    @Column(length = 500)
    private String description; // 방 설명
    @Column(length = 50)
    private String topic; // 방 주제

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner; // 방장

    private GroupChatRoom(String name, String description, String topic, String password, Member owner) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채팅방 이름(name)은 비어 있을 수 없습니다.");
        }
        if (owner == null) {
            throw new IllegalArgumentException("방장(owner)은 null일 수 없습니다.");
        }
        this.name = name;
        this.description = description;
        this.topic = topic;
        this.password = password;
        this.owner = owner;
    }

    // 그룹 채팅방 생성용 정적 팩토리 메서드
    public static GroupChatRoom create(String name, String description, String topic, String password, Member owner) {
        return new GroupChatRoom(name, description, topic, password, owner);
    }

    // 방장 위임
    public void transferOwner(Member newOwner) {
        if (newOwner == null) {
            throw new IllegalArgumentException("새로운 방장(newOwner)은 null일 수 없습니다.");
        }
        this.owner = newOwner;
    }

    // 방장 확인
    public boolean isOwner(Member member) {
        return this.owner.equals(member);
    }

    // 비밀번호 설정 여부 확인
    public boolean hasPassword() {
        return this.password != null && !this.password.trim().isEmpty();
    }

    // 비밀번호 검증
    public void verifyPassword(String inputPassword) {
        if (!hasPassword()) {
            return; // 비밀번호가 설정되지 않은 방은 검증 생략
        }

        if (inputPassword == null || inputPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("채팅방 비밀번호가 필요합니다.");
        }

        if (!this.password.equals(inputPassword.trim())) {
            throw new IllegalArgumentException("채팅방 비밀번호가 올바르지 않습니다.");
        }
    }
}
