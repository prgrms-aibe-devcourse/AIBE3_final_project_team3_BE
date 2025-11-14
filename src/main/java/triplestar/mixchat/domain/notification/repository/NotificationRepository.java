package triplestar.mixchat.domain.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
                select n from Notification n
                join fetch n.receiver.id
                where n.receiver = :receiver
            """
    )
    Page<Notification> findAllByReceiver(Member receiver, Pageable pageable);
}
