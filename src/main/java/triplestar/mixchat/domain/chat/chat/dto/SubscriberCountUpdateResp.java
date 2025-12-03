package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독자 수 변경 응답 (WebSocket 전용)")
public record SubscriberCountUpdateResp(
        @Schema(description = "현재 구독 중인 인원 수", example = "2")
        Integer subscriberCount,

        @Schema(description = "채팅방 전체 멤버 수", example = "4")
        Integer totalMemberCount
) {
    public static SubscriberCountUpdateResp of(Integer subscriberCount, Integer totalMemberCount) {
        return new SubscriberCountUpdateResp(subscriberCount, totalMemberCount);
    }
}
