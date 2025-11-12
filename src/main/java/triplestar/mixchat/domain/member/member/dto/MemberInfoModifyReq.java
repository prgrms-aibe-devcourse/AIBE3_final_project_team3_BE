package triplestar.mixchat.domain.member.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;

@Schema(description = "회원 정보 수정 요청 DTO")
public record MemberInfoModifyReq(
        @Schema(description = "실명")
        @NotNull
        String name,

        @Schema(description = "국가 코드 (ISO 3166 Alpha-2)", example = "KR")
        @NotNull
        String country,

        @Schema(description = "사용자 닉네임", example = "MixMaster")
        @NotNull
        String nickname,

        @Schema(description = "영어 실력 레벨")
        @NotNull
        EnglishLevel englishLevel,

        @Schema(description = "관심사", example = "요리, 여행")
        @NotNull
        List<String> interest,

        @Schema(description = "자기소개")
        @NotNull
        String description
) {
}
