package triplestar.mixchat.domain.chat.dto;

import triplestar.mixchat.domain.chat.entity.ChatRoom;
import triplestar.mixchat.domain.member.member.entity.Member;

import java.util.List;
import java.util.stream.Collectors;

public record ChatRoomResponseDto(
        Long id,
        String name,
        ChatRoom.RoomType roomType,
        List<MemberDto> members
) {
    public static ChatRoomResponseDto from(ChatRoom entity) {
        List<MemberDto> memberDtos = entity.getMembers().stream()
                .map(chatMember -> MemberDto.from(chatMember.getMember()))
                .collect(Collectors.toList());

        return new ChatRoomResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getRoomType(),
                memberDtos
        );
    }

    public record MemberDto(Long id, String nickname) {
        public static MemberDto from(Member member) {
            return new MemberDto(member.getId(), member.getNickname());
        }
    }
}
