package triplestar.mixchat.global.jwt;

import triplestar.mixchat.domain.member.member.constant.Role;

public record AccessTokenPayload(
        Long memberId,
        Role role
) {
}
