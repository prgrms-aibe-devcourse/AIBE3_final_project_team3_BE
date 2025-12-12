package triplestar.mixchat.domain.member.member.repository;

import static triplestar.mixchat.domain.member.member.constant.Role.ROLE_MEMBER;
import static triplestar.mixchat.domain.member.member.entity.QMember.member;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> findAllByIdIsNot(Long id, Pageable pageable) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageable);

        List<Member> results = queryFactory
                .selectFrom(member)
                .where(
                        member.id.ne(id),
                        member.isDeleted.isFalse(),
                        member.isBlocked.isFalse(),
                        member.role.eq(ROLE_MEMBER)
                )
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        member.id.ne(id),
                        member.isDeleted.isFalse(),
                        member.isBlocked.isFalse(),
                        member.role.eq(ROLE_MEMBER)
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        // 기본 정렬: 최근 활동 순
        if (pageable.getSort().isEmpty()) {
            return new OrderSpecifier[]{member.lastSeenAt.desc()};
        }

        return pageable.getSort().stream()
                .map(order -> {
                    PathBuilder<Member> pathBuilder = new PathBuilder<>(Member.class, member.getMetadata());
                    if (order.isAscending()) {
                        return new OrderSpecifier(Order.ASC, pathBuilder.get(order.getProperty()));
                    } else {
                        return new OrderSpecifier(Order.DESC, pathBuilder.get(order.getProperty()));
                    }
                })
                .toArray(OrderSpecifier[]::new);
    }

    @Override
    public Page<Member> findByIds(Long currentUserId, List<Long> onlineMemberIds, Pageable pageable) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageable);

        List<Member> results = queryFactory
                .selectFrom(member)
                .where(
                        member.id.ne(currentUserId),
                        member.id.in(onlineMemberIds),
                        member.isDeleted.isFalse(),
                        member.isBlocked.isFalse(),
                        member.role.eq(ROLE_MEMBER)
                )
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        member.id.ne(currentUserId),
                        member.id.in(onlineMemberIds),
                        member.isDeleted.isFalse(),
                        member.isBlocked.isFalse(),
                        member.role.eq(ROLE_MEMBER)
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }

    @Override
    public void updateLastSeenAt(Long memberId, LocalDateTime lastSeenAt) {
        queryFactory.update(member)
                .set(member.lastSeenAt, lastSeenAt)
                .where(member.id.eq(memberId))
                .execute();
    }
}
