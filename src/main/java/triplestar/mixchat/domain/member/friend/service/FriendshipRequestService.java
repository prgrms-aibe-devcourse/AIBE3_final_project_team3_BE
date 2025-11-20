package triplestar.mixchat.domain.member.friend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.friend.entity.FriendshipRequest;
import triplestar.mixchat.domain.member.friend.repository.FriendshipRequestRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.notifiaction.NotificationEvent;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendshipRequestService {

    private final FriendshipService friendshipService;
    private final FriendshipRequestRepository friendshipRequestRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 친구 요청 생성
     */
    public Long sendRequest(Long senderId, Long receiverId) {
        Member sender = findMemberById(senderId);
        Member receiver = findMemberById(receiverId);
        validateFriendshipRequest(sender, receiver);

        FriendshipRequest friendshipRequest = new FriendshipRequest(sender, receiver);
        FriendshipRequest entity = friendshipRequestRepository.save(friendshipRequest);

        // 알림 이벤트 발행
        eventPublisher.publishEvent(new NotificationEvent(receiverId, senderId, NotificationType.FRIEND_REQUEST));
        return entity.getId();
    }

    private void validateFriendshipRequest(Member sender, Member receiver) {
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
        boolean friendshipExists = friendshipService.isFriends(sender.getId(), receiver.getId());
        if (friendshipExists) {
            throw new IllegalStateException("이미 친구 관계입니다.");
        }

        boolean requestExists = friendshipRequestRepository
                .existsBySenderAndReceiver(sender, receiver);
        boolean reverseRequestExists = friendshipRequestRepository
                .existsBySenderAndReceiver(receiver, sender);
        if (requestExists || reverseRequestExists) {
            throw new IllegalStateException("이미 친구 요청이 존재합니다.");
        }
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
    }

    /**
     * 친구 요청 처리
     */
    public void processRequest(Long memberId, Long friendshipRequestId, boolean isAccepted) {
        FriendshipRequest request = validateFriendshipRequest(memberId, friendshipRequestId);

        if (isAccepted) {
            friendshipService.createFriendship(request.getSender(), request.getReceiver());
        }

        friendshipRequestRepository.delete(request);

        // 알림 이벤트 발행
        publishRequestResult(isAccepted, request);
    }

    private FriendshipRequest validateFriendshipRequest(Long memberId, Long requestId) {
        FriendshipRequest request = friendshipRequestRepository.findByIdWithSenderReceiver(requestId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 친구 요청입니다."));

        if (!request.getReceiver().getId().equals(memberId)) {
            throw new AccessDeniedException("본인이 받은 친구 요청만 처리할 수 있습니다.");
        }
        return request;
    }

    private void publishRequestResult(boolean isAccepted, FriendshipRequest request) {
        if (isAccepted) {
            eventPublisher.publishEvent(new NotificationEvent(
                    request.getSender().getId(),
                    request.getReceiver().getId(),
                    NotificationType.FRIEND_REQUEST_ACCEPT
            ));
        } else {
            eventPublisher.publishEvent(new NotificationEvent(
                    request.getSender().getId(),
                    request.getReceiver().getId(),
                    NotificationType.FRIEND_REQUEST_REJECT
            ));
        }
    }
}
