package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "공개 그룹 채팅방 요약 (Find 페이지 조회용 - 최적화)")
public record GroupChatRoomPublicResp(
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "채팅방 이름", example = "영어 스터디", requiredMode = REQUIRED)
        String name,

        @Schema(description = "채팅방 설명", example = "초급자 환영!")
        String description,

        @Schema(description = "채팅방 주제", example = "영어 회화")
        String topic,

        @Schema(description = "비밀번호 설정 여부", example = "false", requiredMode = REQUIRED)
        boolean hasPassword,

        @Schema(description = "현재 멤버 수", example = "5", requiredMode = REQUIRED)
        Long memberCount
) {
    public static GroupChatRoomPublicResp from(GroupChatRoom room, Long memberCount) {
        return new GroupChatRoomPublicResp(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getTopic(),
                room.getPassword() != null && !room.getPassword().isEmpty(),
                memberCount
        );
    }
}
