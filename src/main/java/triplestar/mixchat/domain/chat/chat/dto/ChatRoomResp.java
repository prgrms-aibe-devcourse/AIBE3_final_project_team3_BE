package triplestar.mixchat.domain.chat.chat.dto;


import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.member.member.entity.Member;

public record ChatRoomResp(
        @Schema(description = "채팅방 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "채팅방 이름", example = "그룹 채팅방", requiredMode = REQUIRED)
        String name,

        @Schema(description = "채팅방 타입", example = "GROUP", requiredMode = REQUIRED)
        ChatRoom.RoomType roomType,

        @Schema(description = "채팅방 멤버 목록", requiredMode = REQUIRED)
        List<MemberDto> members
) {
    public static ChatRoomResp from(ChatRoom entity) {
        List<MemberDto> memberDtos = entity.getMembers().stream()
                .map(chatMember -> MemberDto.from(chatMember.getMember()))
                .collect(Collectors.toList());

        return new ChatRoomResp(
                entity.getId(),
                entity.getName(),
                entity.getRoomType(),
                memberDtos
        );
    }

    @Schema(description = "채팅방 멤버 정보")
    public record MemberDto(
            @NotNull
            @Schema(description = "멤버 ID", example = "1")
            Long id,

            @NotNull
            @Schema(description = "멤버 닉네임", example = "JohnDoe")
            String nickname
    ) {
        // from 대신 of도 고려
        public static MemberDto from(Member member) {
            return new MemberDto(member.getId(), member.getNickname());
        }
    }
}
