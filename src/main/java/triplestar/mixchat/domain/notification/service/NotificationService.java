package triplestar.mixchat.domain.notification.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.dto.NotificationReq;
import triplestar.mixchat.domain.notification.dto.NotificationResp;
import triplestar.mixchat.domain.notification.entity.Notification;
import triplestar.mixchat.domain.notification.repository.NotificationRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원 ID 없음: " + memberId));
    }

    private Notification findNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 알람 ID 없음: " + id));
    }

    public NotificationResp createNotification(NotificationReq req) {
        Member receiver = findMemberById(req.receiverId());

        Notification notification = new Notification(
                receiver,
                req.type(),
                req.content()
        );

        Notification savedNotification = notificationRepository.save(notification);

        return new NotificationResp(
                savedNotification.getId(),
                savedNotification.getReceiver().getId(),
                savedNotification.getType(),
                savedNotification.getContent(),
                savedNotification.getCreatedAt()
        );
    }

    public Page<NotificationResp> getNotifications(Long memberId, Pageable pageable) {
        Member receiver = findMemberById(memberId);

        Page<Notification> notifications = notificationRepository.findAllByReceiver(receiver, pageable);

        return notifications.map(notification -> new NotificationResp(
                notification.getId(),
                notification.getReceiver().getId(),
                notification.getType(),
                notification.getContent(),
                notification.getCreatedAt()
        ));
    }

    public void markAsRead(Long id) {
        findNotificationById(id).read();
    }

    public void markAllAsRead(Long memberId) {
        Member receiver = findMemberById(memberId);
        Page<Notification> notifications = notificationRepository.findAllByReceiver(receiver, Pageable.unpaged());
        notifications.forEach(Notification::read);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public void deleteAllNotifications(Long memberId) {
        Member receiver = findMemberById(memberId);
        Page<Notification> notifications = notificationRepository.findAllByReceiver(receiver, Pageable.unpaged());
        notificationRepository.deleteAll(notifications.getContent());
    }

    // TODO: 읽은 알람만 삭제
    public void deleteReadNotifications(Long memberId) {
    }

    // TODO: 일정 기간 지난 알람 삭제, 기준일수 주입받기
    public void deleteOldNotifications() {

    }
}
