package triplestar.mixchat.domain.member.member.dto;

public record MemberSummaryResp(
        String email,
        String password,
        String name,
        String country,
        String nickname,
        String englishLevel,
        String interest,
        String description
) {
}
