package triplestar.mixchat.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
}
