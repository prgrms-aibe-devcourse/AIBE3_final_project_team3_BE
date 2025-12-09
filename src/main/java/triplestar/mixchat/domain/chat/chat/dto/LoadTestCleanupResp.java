package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "부하테스트 데이터 정리 결과")
public record LoadTestCleanupResp(
        @Schema(description = "삭제된 Direct 채팅방 수", example = "45")
        int deletedDirectRooms,

        @Schema(description = "삭제된 Group 채팅방 수", example = "15")
        int deletedGroupRooms,

        @Schema(description = "삭제된 AI 채팅방 수", example = "5")
        int deletedAIRooms,

        @Schema(description = "삭제된 채팅방 멤버 레코드 수", example = "320")
        int deletedMembers,

        @Schema(description = "삭제된 메시지 수 (MongoDB)", example = "12543")
        long deletedMessages,

        @Schema(description = "Dry Run 모드 여부", example = "false")
        boolean dryRun,

        @Schema(description = "삭제 작업 소요 시간(ms)", example = "1234")
        long elapsedTimeMs
) {
    public static LoadTestCleanupResp of(
            int directRooms,
            int groupRooms,
            int aiRooms,
            int members,
            long messages,
            boolean dryRun,
            long elapsedTimeMs
    ) {
        return new LoadTestCleanupResp(
                directRooms,
                groupRooms,
                aiRooms,
                members,
                messages,
                dryRun,
                elapsedTimeMs
        );
    }

    public int getTotalRooms() {
        return deletedDirectRooms + deletedGroupRooms + deletedAIRooms;
    }
}
