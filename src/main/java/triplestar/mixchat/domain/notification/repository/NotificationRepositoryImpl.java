package triplestar.mixchat.domain.notification.repository;

import static triplestar.mixchat.domain.notification.entity.QNotification.notification;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.member.entity.QMember;
import triplestar.mixchat.domain.notification.entity.Notification;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Notification> findAllByReceiverId(Long receiverId, Pageable pageable) {
        QMember receiver = new QMember("receiver");
        QMember sender = new QMember("sender");

        List<Notification> results = queryFactory
                .selectFrom(notification)
                .join(notification.receiver, receiver)
                .leftJoin(notification.sender, sender).fetchJoin()
                .where(receiver.id.eq(receiverId))
                .orderBy(orderByCreatedAtDesc(), orderByIdDesc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(notification.count())
                .from(notification)
                .join(notification.receiver, receiver)
                .where(receiver.id.eq(receiverId))
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }

    private OrderSpecifier<?> orderByCreatedAtDesc() {
        return new OrderSpecifier<>(Order.DESC, notification.createdAt);
    }

    private OrderSpecifier<?> orderByIdDesc() {
        return new OrderSpecifier<>(Order.DESC, notification.id);
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        queryFactory.update(notification)
                .set(notification.isRead, true)
                .where(notification.receiver.id.eq(receiverId))
                .execute();
    }

    @Override
    public void deleteAllByReceiver(Long receiverId) {
        queryFactory.delete(notification)
                .where(notification.receiver.id.eq(receiverId))
                .execute();
    }

    @Override
    public void deleteOld(LocalDateTime threshold) {
        queryFactory.delete(notification)
                .where(notification.createdAt.lt(threshold))
                .execute();
    }
}