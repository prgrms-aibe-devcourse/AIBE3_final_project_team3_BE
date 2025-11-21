package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom; // GroupChatRoom 엔티티 사용
import triplestar.mixchat.domain.chat.chat.entity.ChatMember; // ChatMember 엔티티 사용

public record GroupChatRoomResp( // 클래스 이름 변경
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "채팅방 이름", example = "그룹 채팅방", requiredMode = REQUIRED)
        String name,

        @Schema(description = "채팅방 설명", example = "스터디 그룹입니다.", requiredMode = REQUIRED)
        String description,

        @Schema(description = "채팅방 주제", example = "스터디", requiredMode = REQUIRED)
        String topic,

        @Schema(description = "채팅방 멤버 목록", requiredMode = REQUIRED)
        List<MemberDto> members // 추출된 MemberDto 사용
) {
    public static GroupChatRoomResp from(GroupChatRoom entity, List<ChatMember> chatMembers) { // from 메서드 시그니처 변경
        List<MemberDto> memberDtos = chatMembers.stream()
                .map(chatMember -> MemberDto.from(chatMember.getMember()))
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
