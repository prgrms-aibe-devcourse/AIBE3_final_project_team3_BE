package triplestar.mixchat.domain.notification.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.notifiaction.NotificationEvent;
import triplestar.mixchat.domain.notification.dto.NotificationResp;
import triplestar.mixchat.domain.notification.entity.Notification;
import triplestar.mixchat.domain.notification.repository.NotificationRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    @Value("${notification.retention-days}")
    private int retentionDays;

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
    @Transactional
    public NotificationResp createNotification(NotificationEvent event) {
        Member receiver = memberRepository.findById(event.receiverId())
                .orElseThrow(() -> new EntityNotFoundException("해당 회원 ID 없음"));

        Member sender = null;
        if (event.senderId() != null) {
            sender = memberRepository.findById(event.senderId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 회원 ID 없음"));
        }

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(event.type())
                .content(event.extraContent()).build();

        Notification saved = notificationRepository.save(notification);

        return new NotificationResp(
                saved.getId(),
                saved.getReceiver().getId(),
                saved.getSender() == null ? null : saved.getSender().getId(),
                saved.getSender() == null ? null : saved.getSender().getNickname(),
                saved.getType(),
                saved.isRead(),
                saved.getCreatedAt(),
                saved.getContent()
        );
    }

    /**
     * 알림 조회(페이징)
     */
    public Page<NotificationResp> getNotifications(Long receiverId, Pageable pageable) {
        return notificationRepository.findAllByReceiverId(receiverId, pageable);
    }

    /**
     * 수정(읽음 처리)
     */
    @Transactional
    public void markAsRead(Long memberId, Long id) {
        findNotificationById(memberId, id).read();
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsRead(memberId);
    }

    /**
     * 삭제
     */
    @Transactional
    public void deleteNotification(Long memberId, Long id) {
        Notification notification = findNotificationById(memberId, id);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(Long memberId) {
        notificationRepository.deleteAllByReceiver(memberId);
    }

    // 매일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledDeleteOldNotifications() {
        deleteOldNotifications();
    }

    private void deleteOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        notificationRepository.deleteOld(threshold);
    }
}
