package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;

@Schema(description = "채팅방 멤버 업데이트 응답")
public record RoomMemberUpdateResp(
    @Schema(description = "채팅방 ID", example = "1")
    Long roomId,
    @Schema(description = "업데이트 타입", example = "JOIN")
    String type, // JOIN, LEAVE, KICK
    @Schema(description = "멤버 요약 정보")
    MemberSummaryResp member,
    @Schema(description = "총 멤버 수", example = "5")
    int totalMemberCount,
    @Schema(description = "구독 중인 멤버 수", example = "3")
    int subscriberCount
) {}