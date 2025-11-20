package triplestar.mixchat.domain.member.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;

@Schema(description = "회원 정보 수정 요청 DTO")
public record MemberInfoModifyReq(
        @Schema(description = "실명")
        @NotBlank
        String name,

        @Schema(description = "국가 코드 (ISO 3166 Alpha-2)", example = "KR")
        @NotNull
        Country country,

        @Schema(description = "사용자 닉네임", example = "MixMaster")
        @NotBlank
        String nickname,

        @Schema(description = "영어 실력 레벨")
        @NotNull
        EnglishLevel englishLevel,

        @Schema(description = "관심사", example = "요리, 여행")
        @NotEmpty
        List<String> interests,

        @Schema(description = "자기소개")
        @NotBlank
        String description
) {
}
