package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;

@Schema(description = "그룹 채팅방 응답")
public record GroupChatRoomResp( // 클래스 이름 변경
        @NotNull
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @NotBlank
        @Schema(description = "채팅방 이름", example = "그룹 채팅방", requiredMode = REQUIRED)
        String name,

        @NotBlank
        @Schema(description = "채팅방 설명", example = "스터디 그룹입니다.", requiredMode = REQUIRED)
        String description,

        @NotBlank
        @Schema(description = "채팅방 주제", example = "스터디", requiredMode = REQUIRED)
        String topic,

        @NotEmpty
        @Schema(description = "채팅방 멤버 목록", requiredMode = REQUIRED)
        List<ChatMemberResp> members // 추출된 MemberDto 사용
) {
    public static GroupChatRoomResp from(GroupChatRoom entity, List<ChatMember> chatMembers) { // from 메서드 시그니처 변경
        List<ChatMemberResp> memberDtos = chatMembers.stream()
                .map(chatMember -> ChatMemberResp.from(chatMember.getMember()))
                .collect(Collectors.toList());

        return new GroupChatRoomResp(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTopic(),
                memberDtos
        );
    }
}
