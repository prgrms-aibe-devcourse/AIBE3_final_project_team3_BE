package triplestar.mixchat.domain.member.presence.dto;

public record ExpiredPresence(
        Long memberId,
        Long lastSeenAt
) {
}
