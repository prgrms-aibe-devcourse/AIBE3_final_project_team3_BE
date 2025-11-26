package triplestar.mixchat.domain.member.member.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "멤버 조회 시 반환되는 요약된 사용자 정보와 온라인 여부 정보")
public record MemberPresenceSummaryResp(
        @Schema(description = "사용자 고유 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "사용자 닉네임", example = "MixMaster", requiredMode = REQUIRED)
        String nickname,

        @Schema(description = "국가 코드 (Alpha-2)", example = "KR", requiredMode = REQUIRED)
        String country,

        @Schema(description = "영어 실력 레벨", example = "INTERMEDIATE", requiredMode = REQUIRED)
        String englishLevel,

        @Schema(description = "관심사 목록", example = "TRAVEL, FOOD", requiredMode = REQUIRED)
        List<String> interests,

        @Schema(description = "자기소개", example = "안녕하세요.", requiredMode = REQUIRED)
        String description,

        @Schema(description = "온라인 상태 여부", example = "true", requiredMode = REQUIRED)
        boolean isOnline
) {
    public static MemberPresenceSummaryResp from(Member savedMember, boolean isOnline) {
        return new MemberPresenceSummaryResp(
                savedMember.getId(),
                savedMember.getNickname(),
                savedMember.getCountry().name(),
                savedMember.getEnglishLevel().name(),
                savedMember.getInterests(),
                savedMember.getDescription(),
                isOnline
        );
    }
}
