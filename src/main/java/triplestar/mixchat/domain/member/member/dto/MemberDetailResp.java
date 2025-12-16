package triplestar.mixchat.domain.member.member.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import triplestar.mixchat.domain.member.friend.dto.FriendshipStateInfo;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.domain.member.member.entity.Member;

@Schema(description = "회원 상세 조회 응답 DTO")
public record MemberDetailResp(
        @Schema(description = "회원 고유 ID", example = "123", requiredMode = REQUIRED)
        Long memberId,

        @Schema(description = "닉네임", example = "MixMaster", requiredMode = REQUIRED)
        String nickname,

        @Schema(description = "국가 코드 (Alpha-2)", example = "KR", requiredMode = REQUIRED)
        String country,

        @Schema(description = "영어 실력 레벨", example = "BEGINNER", requiredMode = REQUIRED)
        String englishLevel,

        @Schema(description = "관심사 목록", example = "[\"농구\", \"독서\"]", requiredMode = REQUIRED)
        List<String> interests,

        @Schema(description = "자기소개", example = "안녕하세요. 5년차 웹 개발자입니다.", requiredMode = REQUIRED)
        String description,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/123_photo.jpg", requiredMode = REQUIRED)
        String profileImageUrl,

        @Schema(description = "마지막으로 온라인 상태였던 시간 (ISO 8601 형식)",
                example = "2024-10-01T14:30:00", requiredMode = REQUIRED)
        LocalDateTime lastSeenAt,

        @Schema(description = "회원 역할", example = "MEMBER", requiredMode = REQUIRED)
        Role role,

        @Schema(description = "현재 로그인 사용자와 친구 관계인지 여부", example = "true", requiredMode = REQUIRED)
        boolean isFriend,

        @Schema(description = "현재 로그인 사용자가 상대방에게 친구 요청을 보내고 대기 중인 상태인지 여부",
                example = "true")
        boolean isFriendRequestSent,

        @Schema(description = "상대방이 현재 사용자에게 보낸 친구 요청의 ID. 대기 중인 요청이 없으면 null입니다.",
                example = "51")
        Long receivedFriendRequestId
) {
    public static MemberDetailResp forAnonymousViewer(Member member) {
        return new MemberDetailResp(
                member.getId(),
                member.getNickname(),
                member.getCountry().name(),
                member.getEnglishLevel().name(),
                member.getInterests(),
                member.getDescription(),
                member.getProfileImageUrl(),
                member.getLastSeenAt(),
                member.getRole(),
                false,
                false,
                null
        );
    }

    public static MemberDetailResp from(Member member, FriendshipStateInfo friendshipStateInfo) {
        return new MemberDetailResp(
                member.getId(),
                member.getNickname(),
                member.getCountry().name(),
                member.getEnglishLevel().name(),
                member.getInterests(),
                member.getDescription(),
                member.getProfileImageUrl(),
                member.getLastSeenAt(),
                member.getRole(),
                friendshipStateInfo.isFriend(),
                friendshipStateInfo.isFriendRequestSent(),
                friendshipStateInfo.receivedFriendRequestId()
        );
    }
}
