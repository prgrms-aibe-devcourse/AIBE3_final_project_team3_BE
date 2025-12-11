package triplestar.mixchat.domain.chat.chat.service;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.GroupChatRoomResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.friend.repository.FriendshipRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;
import triplestar.mixchat.global.cache.ChatSubscriberCacheService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupChatRoomService {

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatAuthCacheService chatAuthCacheService;
    private final ChatSubscriberCacheService chatSubscriberCacheService;
    private final ChatMemberService chatMemberService;
    private final ChatMessageService chatMessageService;
    private final FriendshipRepository friendshipRepository;
    private final SystemMessageService systemMessageService;
    private final triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository chatMessageRepository;
    private final PasswordEncoder passwordEncoder;

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    @Transactional
    public GroupChatRoomResp createGroupRoom(CreateGroupChatReq request, Long creatorId) {
        Member creator = findMemberById(creatorId);

        String encryptedPassword = request.password();
        if (request.password() != null && !request.password().trim().isEmpty()) {
            encryptedPassword = passwordEncoder.encode(request.password());
        }

        // 방장(owner)을 creator로 설정하여 방 생성
        GroupChatRoom newRoom = GroupChatRoom.create(request.roomName(), request.description(), request.topic(), encryptedPassword, creator);

        List<Member> members = memberRepository.findAllById(request.memberIds());
        if (!members.contains(creator)) {
            members.add(creator);
        }

        GroupChatRoom savedRoom = groupChatRoomRepository.save(newRoom);

        // 모든 멤버를 ChatMember로 추가 (방장 여부는 GroupChatRoom.owner로 판단)
        List<ChatMember> chatMembers = members.stream().map(member ->
            new ChatMember(member, savedRoom.getId(), ChatRoomType.GROUP)
        ).collect(Collectors.toList());
        chatRoomMemberRepository.saveAll(chatMembers);

        chatMembers.forEach(cm ->
                chatAuthCacheService.addMember(savedRoom.getId(), cm.getMember().getId())
        );

        // 친구 목록 조회
        // NOTE : 메소드 type 변환으로 인한 임시 처리
        Page<Member> friendsByMemberId = friendshipRepository.findFriendsByMemberId(creatorId, Pageable.ofSize(500));
        Page<Long> friendIdPage = friendsByMemberId.map(Member::getId);
        Set<Long> friendIdSet = new HashSet<>(friendIdPage.getContent());

        GroupChatRoomResp roomDto = GroupChatRoomResp.from(savedRoom, chatMembers, creatorId, friendIdSet, 0L, null);
        members.forEach(member -> {
            messagingTemplate.convertAndSendToUser(member.getId().toString(), "/queue/rooms", roomDto);
        });

        return roomDto;
    }

    @Transactional
    public void leaveRoom(Long roomId, Long currentUserId) {
        // 1. 채팅방 조회
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹 채팅방입니다."));

        // 2. ChatMember 찾기
        ChatMember memberToRemove = chatRoomMemberRepository
                .findByChatRoomIdAndChatRoomTypeAndMember_Id(roomId, ChatRoomType.GROUP, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("해당 대화방에 접근할 권한이 없습니다."));

        String leavingMemberNickname = memberToRemove.getMember().getNickname();

        // 3. 방장이 나가는 경우 방장 위임 처리
        if (room.isOwner(memberToRemove.getMember())) {
            // 남은 멤버 조회 (본인 제외)
            List<ChatMember> remainingMembers = chatRoomMemberRepository
                    .findByChatRoomIdAndChatRoomType(roomId, ChatRoomType.GROUP)
                    .stream()
                    .filter(cm -> !cm.getMember().getId().equals(currentUserId))
                    .collect(Collectors.toList());

            // 남은 멤버가 있으면 가장 먼저 참가한 사람에게 방장 위임
            if (!remainingMembers.isEmpty()) {
                ChatMember newOwnerMember = remainingMembers.stream()
                        .min((cm1, cm2) -> cm1.getCreatedAt().compareTo(cm2.getCreatedAt()))
                        .orElseThrow();
                room.transferOwner(newOwnerMember.getMember());
                log.info("방장이 나가서 새로운 방장으로 위임되었습니다. 방 ID: {}, 새 방장 ID: {}",
                        roomId, newOwnerMember.getMember().getId());
            }
        }

        // 4. ChatMember 삭제
        chatRoomMemberRepository.delete(memberToRemove);

        // 5. 캐시에서 멤버 제거
        chatAuthCacheService.removeMember(roomId, currentUserId);

        // Redis 구독자 캐시에서 세션 제거
        Set<String> memberSessions = chatSubscriberCacheService.getSessionsByMemberId(roomId, currentUserId);
        if (memberSessions != null) {
            for (String sessionId : memberSessions) {
                chatSubscriberCacheService.removeSubscriber(roomId, currentUserId, sessionId);
            }
        }

        // 6. 남은 멤버 수 확인 후 대화방 삭제
        long remainingMembersCount = chatRoomMemberRepository.countByChatRoomIdAndChatRoomType(
                roomId, ChatRoomType.GROUP);

        if (remainingMembersCount == 0) {
            groupChatRoomRepository.deleteById(roomId);
            log.info("모든 멤버가 나가 그룹 채팅방이 삭제되었습니다. 방 ID: {}", roomId);
        } else {
            // 7. 남은 멤버가 있으면 시스템 메시지 전송
            systemMessageService.sendLeaveMessage(roomId, leavingMemberNickname, ChatRoomType.GROUP);
            // 멤버 업데이트 브로드캐스트
            chatMemberService.broadcastMemberUpdate(roomId, ChatRoomType.GROUP, memberToRemove.getMember(), "LEAVE");
        }
    }


    @Transactional
    public GroupChatRoomResp joinGroupRoom(Long roomId, Long userId, String password) {
        // 1. 채팅방 존재 여부 확인
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. ID: " + roomId));

        // 2. 이미 참가한 회원인지 확인
        boolean alreadyMember = chatRoomMemberRepository.existsByChatRoomIdAndChatRoomTypeAndMember_Id(
                roomId, ChatRoomType.GROUP, userId);
        if (alreadyMember) {
            throw new IllegalStateException("이미 참가한 채팅방입니다.");
        }

        // 3. 비밀번호 검증
        room.verifyPassword(password, passwordEncoder);

        // 4. 회원 조회
        Member member = findMemberById(userId);

        // 5. ChatMember 추가
        ChatMember newMember = new ChatMember(member, roomId, ChatRoomType.GROUP);
        chatRoomMemberRepository.save(newMember);

        // 6. 캐시에 추가
        chatAuthCacheService.addMember(roomId, userId);

        // 7. 응답 생성 (해당 방의 모든 멤버 정보 포함)
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, ChatRoomType.GROUP);
        
        // 친구 목록 조회
        // NOTE : 메소드 type 변환으로 인한 임시 처리
        Page<Member> friendsByMemberId = friendshipRepository.findFriendsByMemberId(userId, Pageable.ofSize(500));
        Page<Long> friendIdPage = friendsByMemberId.map(Member::getId);
        Set<Long> friendIdSet = new HashSet<>(friendIdPage.getContent());

        GroupChatRoomResp roomDto = GroupChatRoomResp.from(room, allMembers, userId, friendIdSet, 0L, null);

        // 8. 시스템 메시지 전송 (새 멤버 입장 알림)
        systemMessageService.sendJoinMessage(roomId, member.getNickname(), ChatRoomType.GROUP);
        // 멤버 업데이트 브로드캐스트
        chatMemberService.broadcastMemberUpdate(roomId, ChatRoomType.GROUP, member, "JOIN");

        // 9. WebSocket으로 방의 모든 멤버에게 알림 (방 정보 업데이트)
        allMembers.forEach(cm -> {
            messagingTemplate.convertAndSendToUser(
                    cm.getMember().getId().toString(),
                    "/queue/rooms/update",
                    roomDto
            );
        });

        return roomDto;
    }

    @Transactional
    public void inviteMember(Long roomId, Long inviterId, Long targetMemberId) {
        // 1. 채팅방 조회
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 2. 초대하는 사람이 채팅방 멤버인지 확인
        boolean isInviterMember = chatRoomMemberRepository.existsByChatRoomIdAndChatRoomTypeAndMember_Id(
                roomId, ChatRoomType.GROUP, inviterId);
        if (!isInviterMember) {
            throw new AccessDeniedException("초대 권한이 없습니다. 채팅방 멤버만 초대할 수 있습니다.");
        }

        // 3. 초대 대상이 이미 멤버인지 확인
        boolean isTargetAlreadyMember = chatRoomMemberRepository.existsByChatRoomIdAndChatRoomTypeAndMember_Id(
                roomId, ChatRoomType.GROUP, targetMemberId);
        if (isTargetAlreadyMember) {
            throw new IllegalStateException("이미 채팅방에 참여중인 멤버입니다.");
        }

        // 4. 대상 멤버 조회
        Member targetMember = findMemberById(targetMemberId);

        // 5. ChatMember 추가
        ChatMember newMember = new ChatMember(targetMember, roomId, ChatRoomType.GROUP);
        chatRoomMemberRepository.save(newMember);

        // 6. 캐시 업데이트
        chatAuthCacheService.addMember(roomId, targetMemberId);

        // 7. 시스템 메시지 전송 (초대 메시지)
        Member inviter = findMemberById(inviterId);
        systemMessageService.sendInviteMessage(roomId, inviter.getNickname(), targetMember.getNickname(), ChatRoomType.GROUP);

        // 8. 멤버 업데이트 브로드캐스트
        chatMemberService.broadcastMemberUpdate(roomId, ChatRoomType.GROUP, targetMember, "JOIN");
        
        // 9. 초대된 멤버에게 방 정보 전송 (Optional but good UX)
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, ChatRoomType.GROUP);
        Page<Member> friendsByMemberId = friendshipRepository.findFriendsByMemberId(targetMemberId, Pageable.ofSize(500));
        Page<Long> friendIdPage = friendsByMemberId.map(Member::getId);
        Set<Long> friendIdSet = new HashSet<>(friendIdPage.getContent());
        
        GroupChatRoomResp roomDto = GroupChatRoomResp.from(room, allMembers, targetMemberId, friendIdSet, 0L, null);
        messagingTemplate.convertAndSendToUser(targetMemberId.toString(), "/queue/rooms", roomDto);
    }

    // 사용자가 속해있는 그룹채팅방 조회(chat 페이지 용도)
    public List<GroupChatRoomResp> getRoomsForUser(Long currentUserId) {
        List<GroupChatRoom> rooms = groupChatRoomRepository.findAllByMemberId(currentUserId);
        return convertToRoomResponses(rooms, currentUserId);
    }

    // 기존에 만들어진 그룹채팅방 조회(find 페이지의 Groups 탭 용도)
    public List<GroupChatRoomResp> getGroupPublicRooms(Long currentUserId) {
        List<GroupChatRoom> rooms = groupChatRoomRepository.findPublicRoomsExcludingMemberId(currentUserId);
        return convertToRoomResponses(rooms, currentUserId);
    }

    @Transactional
    public void kickMember(Long roomId, Long ownerId, Long targetMemberId) {
        // 1. 채팅방 조회
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹 채팅방입니다."));

        // 2. 요청자가 방장인지 확인
        if (!room.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("멤버를 강퇴할 권한이 없습니다.");
        }

        // 3. 자신을 강퇴할 수 없음
        if (ownerId.equals(targetMemberId)) {
            throw new IllegalArgumentException("자기 자신을 강퇴할 수 없습니다.");
        }

        // 4. 강퇴할 ChatMember 찾기
        ChatMember memberToKick = chatRoomMemberRepository
                .findByChatRoomIdAndChatRoomTypeAndMember_Id(roomId, ChatRoomType.GROUP, targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 존재하지 않는 멤버입니다."));

        String targetNickname = memberToKick.getMember().getNickname();

        // 5. ChatMember 삭제
        chatRoomMemberRepository.delete(memberToKick);

        // 6. 캐시에서 멤버 제거
        chatAuthCacheService.removeMember(roomId, targetMemberId);

        // Redis 구독자 캐시에서 세션 제거
        Set<String> memberSessions = chatSubscriberCacheService.getSessionsByMemberId(roomId, targetMemberId);
        if (memberSessions != null) {
            for (String sessionId : memberSessions) {
                chatSubscriberCacheService.removeSubscriber(roomId, targetMemberId, sessionId);
            }
        }

        // 7. 시스템 메시지 저장 및 전송
        systemMessageService.sendKickMessage(roomId, targetNickname, ChatRoomType.GROUP);
        // 멤버 업데이트 브로드캐스트
        chatMemberService.broadcastMemberUpdate(roomId, ChatRoomType.GROUP, memberToKick.getMember(), "KICK");
    }

    @Transactional
    public void transferOwnership(Long roomId, Long currentOwnerId, Long newOwnerId) {
        // 1. 채팅방과 새로운 방장 멤버 엔티티 조회
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹 채팅방입니다."));
        Member newOwner = memberRepository.findById(newOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));

        // 2. 요청자가 현재 방장인지 확인
        if (!room.getOwner().getId().equals(currentOwnerId)) {
            throw new AccessDeniedException("방장을 위임할 권한이 없습니다.");
        }

        // 3. 새로운 방장이 될 멤버가 현재 채팅방에 속해있는지 확인
        chatRoomMemberRepository.findByChatRoomIdAndChatRoomTypeAndMember_Id(roomId, ChatRoomType.GROUP, newOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("새로운 방장은 채팅방의 멤버여야 합니다."));

        String oldOwnerNickname = room.getOwner().getNickname();
        String newOwnerNickname = newOwner.getNickname();

        // 4. 채팅방의 방장을 새로운 멤버로 변경
        room.transferOwner(newOwner);

        // 5. 시스템 메시지를 저장하고 채팅방 전체에 전송
        systemMessageService.sendOwnerChangedMessage(roomId, oldOwnerNickname, newOwnerNickname, ChatRoomType.GROUP);
    }

    @Transactional
    public void updatePassword(Long roomId, Long userId, String newPassword) {
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹 채팅방입니다."));

        // 방장 권한 확인
        if (!room.isOwner(findMemberById(userId))) {
            throw new AccessDeniedException("비밀번호를 변경할 권한이 없습니다.");
        }

        // 새 비밀번호 암호화 (null이 아니면)
        String encryptedPassword = null;
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            encryptedPassword = passwordEncoder.encode(newPassword);
        }

        room.updatePassword(encryptedPassword);
    }

    //그룹 채팅방 목록을 DTO로 변환
    private List<GroupChatRoomResp> convertToRoomResponses(List<GroupChatRoom> rooms, Long currentUserId) {
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        // 현재 사용자의 친구 ID 목록 조회 (최대 500명으로 가정)
        // NOTE : 메소드 type 변환으로 인한 임시 처리
        Page<Member> friendsByMemberId = friendshipRepository.findFriendsByMemberId(currentUserId,
                Pageable.ofSize(500));
        Page<Long> friendIdPage = friendsByMemberId.map(Member::getId);
        Set<Long> friendIdSet = new HashSet<>(friendIdPage.getContent());

        // 현재 사용자의 모든 그룹 채팅방 ChatMember 정보 조회
        List<ChatMember> currentUserChatMembers = chatRoomMemberRepository.findByMemberAndChatRoomType(findMemberById(currentUserId), ChatRoomType.GROUP);
        Map<Long, Long> lastReadSequenceMap = currentUserChatMembers.stream()
                .collect(Collectors.toMap(ChatMember::getChatRoomId, ChatMember::getLastReadSequence, (seq1, seq2) -> seq1));

        // 1. 모든 방의 ID 수집
        List<Long> roomIds = rooms.stream()
                .map(GroupChatRoom::getId)
                .collect(Collectors.toList());

        // 2. 모든 방의 멤버를 한 번의 쿼리로 조회
        List<ChatMember> allMembers = chatRoomMemberRepository.findAllByRoomIdsWithMember(roomIds);

        // 3. roomId를 키로 하는 Map으로 그룹화
        Map<Long, List<ChatMember>> membersByRoom = allMembers.stream()
                .collect(Collectors.groupingBy(ChatMember::getChatRoomId));

        // 4. DTO 변환
        return rooms.stream()
                .map(room -> {
                    Long lastRead = lastReadSequenceMap.get(room.getId());
                    long unreadCount = (lastRead == null) ? room.getCurrentSequence() : room.getCurrentSequence() - lastRead;
                    if (unreadCount < 0) unreadCount = 0;

                    // 마지막 메시지 조회 (번역된 메시지가 있으면 번역된 내용 사용)
                    String lastMessageContent = chatMessageRepository
                            .findTopByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(room.getId(), ChatRoomType.GROUP)
                            .map(msg -> msg.isTranslateEnabled() && msg.getTranslatedContent() != null
                                    ? msg.getTranslatedContent()
                                    : msg.getContent())
                            .orElse(null);

                    return GroupChatRoomResp.from(
                            room,
                            membersByRoom.getOrDefault(room.getId(), Collections.emptyList()),
                            currentUserId,
                            friendIdSet,
                            unreadCount,
                            lastRead,
                            lastMessageContent
                    );
                })
                .collect(Collectors.toList());
    }
}