package triplestar.mixchat.domain.chat.chat.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.member.member.entity.Member;

import java.util.List;
import java.util.stream.Collectors;

public record ChatRoomResp(
        @NotNull
        @Schema(description = "채팅방 ID", example = "1")
        Long id,

        @NotNull
        @Schema(description = "채팅방 이름", example = "그룹 채팅방")
        String name,

        @NotNull
        @Schema(description = "채팅방 타입", example = "GROUP")
        ChatRoom.RoomType roomType,

        @NotNull
        @Schema(description = "채팅방 멤버 목록")
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

    public record MemberDto(
            @NotNull
            @Schema(description = "멤버 ID", example = "1")
            Long id,

            @NotNull
            @Schema(description = "멤버 닉네임", example = "JohnDoe")
            String nickname
    ) {
        public static MemberDto from(Member member) {
            return new MemberDto(member.getId(), member.getNickname());
        }
    }
}
