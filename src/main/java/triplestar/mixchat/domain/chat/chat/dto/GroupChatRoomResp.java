package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;

@Schema(description = "그룹 채팅방 응답")
public record GroupChatRoomResp(
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "채팅방 이름", example = "그룹 채팅방", requiredMode = REQUIRED)
        String name,

        @Schema(description = "채팅방 설명", example = "스터디 그룹입니다.", requiredMode = REQUIRED)
        String description,

        @Schema(description = "채팅방 주제", example = "스터디", requiredMode = REQUIRED)
        String topic,

        @Schema(description = "비밀번호 설정 여부", example = "true", requiredMode = REQUIRED)
        Boolean hasPassword,

        @Schema(description = "멤버 수", example = "5", requiredMode = REQUIRED)
        Integer memberCount,

        @Schema(description = "생성일시", requiredMode = REQUIRED)
        LocalDateTime createdAt,

        @Schema(description = "방장 ID", requiredMode = REQUIRED)
        Long ownerId,

        @Schema(description = "채팅방 멤버 목록", requiredMode = REQUIRED)
        List<ChatMemberResp> members
) {
    public static GroupChatRoomResp from(GroupChatRoom entity, List<ChatMember> chatMembers) {
        List<ChatMemberResp> memberDtos = chatMembers.stream()
                .map(chatMember -> ChatMemberResp.from(chatMember.getMember()))
                .collect(Collectors.toList());

        // 비밀번호 설정 여부 확인
        boolean hasPassword = entity.getPassword() != null && !entity.getPassword().trim().isEmpty();

        return new GroupChatRoomResp(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTopic(),
                hasPassword,
                memberDtos.size(),
                entity.getCreatedAt(),
                entity.getOwner().getId(),
                memberDtos
        );
    }
}
