package triplestar.mixchat.domain.member.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "회원가입 완료 또는 멤버 조회 시 반환되는 요약된 사용자 정보")
public record MemberSummaryResp(

        @NotNull
        @Schema(description = "사용자 고유 ID", example = "1")
        Long id,

        @NotNull
        @Schema(description = "사용자의 실명", example = "홍길동")
        String name,

        @NotNull
        @Schema(description = "국가 코드 (Alpha-2)", example = "KR")
        String country,

        @NotNull
        @Schema(description = "사용자 닉네임", example = "MixMaster")
        String nickname,

        @NotNull
        @Schema(description = "영어 실력 레벨", example = "INTERMEDIATE")
        String englishLevel,

        @NotNull
        @Schema(description = "관심사 목록", example = "TRAVEL, FOOD")
        List<String> interest,

        @NotNull
        @Schema(description = "자기소개")
        String description
) {
    public MemberSummaryResp(Member savedMember) {
        this(
                savedMember.getId(),
                savedMember.getName(),
                savedMember.getCountry().getCode(),
                savedMember.getNickname(),
                savedMember.getEnglishLevel().name(),
                savedMember.getInterests(),
                savedMember.getDescription()
        );
    }
}
