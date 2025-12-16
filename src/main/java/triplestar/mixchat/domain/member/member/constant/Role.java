package triplestar.mixchat.domain.member.member.constant;

public enum Role {
    ROLE_ADMIN,
    ROLE_MEMBER,
    ROLE_BOT
    ;

    public static boolean isNotMember(Role role) {
        return role != ROLE_MEMBER;
    }
}
