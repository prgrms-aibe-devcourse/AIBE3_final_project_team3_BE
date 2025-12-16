package triplestar.mixchat.domain.notification.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.notification.entity.Notification;

@Repository
public interface NotificationRepositoryCustom {

    Page<Notification> findAllByReceiverId(Long receiverId, Pageable pageable);

    void markAllAsRead(Long receiverId);

    void deleteAllByReceiver(Long receiverId);

    void deleteOld(LocalDateTime threshold);
}
