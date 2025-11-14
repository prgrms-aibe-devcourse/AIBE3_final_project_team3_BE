package triplestar.mixchat.domain.notification.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
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

    private Notification findNotificationById(Long memberId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 알람 ID 없음: " + id));

        // id를 조회할 때는 N+1 문제가 발생하지 않음
        if (!notification.getReceiver().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 회원의 알람이 아님: " + id);
        }

        return notification;
    }

    /**
     * 알림 생성
     */
    public NotificationResp createNotification(NotificationReq req) {
        Member receiver = memberRepository.findById(req.receiverId())
                .orElseThrow(() -> new EntityNotFoundException("해당 회원 ID 없음"));

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

    /**
     * 알림 조회(페이징)
     */
    public Page<NotificationResp> getNotifications(Long receiverId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findAllByReceiverId(receiverId, pageable);

        return notifications.map(notification -> new NotificationResp(
                notification.getId(),
                notification.getReceiver().getId(),
                notification.getType(),
                notification.getContent(),
                notification.getCreatedAt()
        ));
    }

    /**
     * 읽음 처리
     */
    public void markAsRead(Long memberId, Long id) {
        findNotificationById(memberId, id).read();
    }

    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsRead(memberId);
    }

    /**
     * 삭제
     */
    public void deleteNotification(Long memberId, Long id) {
        Notification notification = findNotificationById(memberId, id);
        notificationRepository.delete(notification);
    }

    public void deleteAllNotifications(Long memberId) {
        notificationRepository.deleteAllByReceiver(memberId);
    }

    // TODO: 기준일수 주입받기
    private void deleteOldNotifications() {
        int daysThreshold = 30;
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        notificationRepository.deleteOld(threshold);
    }

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시에 실행
    public void scheduledDeleteOldNotifications() {
        deleteOldNotifications();
    }
}
