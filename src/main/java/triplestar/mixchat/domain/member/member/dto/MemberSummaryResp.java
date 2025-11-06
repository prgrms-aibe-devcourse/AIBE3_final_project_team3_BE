package triplestar.mixchat.domain.member.member.dto;

import triplestar.mixchat.domain.member.member.entity.Member;

public record MemberSummaryResp(
        String name,
        String country,
        String nickname,
        String englishLevel,
        String interest,
        String description
) {
    public MemberSummaryResp(Member savedMember) {
        this(
                savedMember.getName(),
                savedMember.getCountry().getCode(),
                savedMember.getNickname(),
                savedMember.getEnglishLevel().name(),
                savedMember.getInterest(),
                savedMember.getDescription()
        );
    }
}
