package triplestar.mixchat.global.security.jwt;

import triplestar.mixchat.domain.member.member.constant.Role;

public record AccessTokenPayload(
        Long memberId,
        Role role
) {
}
