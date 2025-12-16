package triplestar.mixchat.domain.member.friend.dto;

public record FriendshipStateInfo (
        boolean isFriend,
        boolean isFriendRequestSent,
        Long receivedFriendRequestId
){
}
