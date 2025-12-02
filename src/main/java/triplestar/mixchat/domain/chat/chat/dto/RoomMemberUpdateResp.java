package triplestar.mixchat.domain.chat.chat.dto;

import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;

public record RoomMemberUpdateResp(
    Long roomId,
    String type, // JOIN, LEAVE, KICK
    MemberSummaryResp member,
    int totalMemberCount,
    int subscriberCount
) {}