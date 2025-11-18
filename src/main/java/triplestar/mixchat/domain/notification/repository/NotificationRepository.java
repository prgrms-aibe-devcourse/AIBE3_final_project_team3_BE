package triplestar.mixchat.domain.notification.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.notification.dto.NotificationResp;
import triplestar.mixchat.domain.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
                SELECT new triplestar.mixchat.domain.notification.dto.NotificationResp(
                    n.id,
                    r.id,
                    n.sender.id,
                    n.sender.nickname,
                    n.type,
                    n.isRead,
                    n.createdAt,
                    n.content
                )
                FROM Notification n
                JOIN n.receiver r
                LEFT JOIN n.sender s
                WHERE r.id = :receiverId
            """)
    Page<NotificationResp> findAllByReceiverId(Long receiverId, Pageable pageable);

    @Query("""
                UPDATE Notification n
                SET n.isRead = true
                WHERE n.receiver.id = :receiverId
            """
    )
    @Modifying
    void markAllAsRead(Long receiverId);

    @Query("""
                DELETE FROM Notification n
                WHERE n.receiver.id = :receiverId
            """
    )
    @Modifying
    void deleteAllByReceiver(Long receiverId);

    @Query("""
                DELETE FROM Notification n
                WHERE n.createdAt < :threshold
            """
    )
    @Modifying
    void deleteOld(LocalDateTime threshold);
}
