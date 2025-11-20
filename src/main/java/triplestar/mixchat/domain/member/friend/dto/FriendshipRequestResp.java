package triplestar.mixchat.domain.member.friend.dto;

import triplestar.mixchat.domain.member.friend.entity.FriendshipRequest;

public record FriendshipRequestResp(
        Long id
) {
    public static FriendshipRequestResp from(FriendshipRequest entity) {
        return new FriendshipRequestResp(
                entity.getId()
        );
    }
}
