package triplestar.mixchat.domain.member.friend.repository;

import static triplestar.mixchat.domain.member.friend.entity.QFriendship.friendship;
import static triplestar.mixchat.domain.member.friend.entity.QFriendshipRequest.friendshipRequest;
import static triplestar.mixchat.domain.member.member.entity.QMember.member;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.friend.dto.FriendDetailResp;
import triplestar.mixchat.domain.member.friend.dto.FriendshipStateInfo;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
@RequiredArgsConstructor
public class FriendshipRepositoryImpl implements FriendshipRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> findFriendsByMemberId(Long memberId, Pageable pageable) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageable);

        JPQLQuery<Member> baseQuery = queryFactory
                .select(member)
                .from(friendship)
                .join(member).on(
                        member.id.eq(
                                new CaseBuilder()
                                        .when(friendship.smallerMember.id.eq(memberId)).then(friendship.largerMember.id)
                                        .otherwise(friendship.smallerMember.id)
                        )
                )
                .where(friendship.smallerMember.id.eq(memberId)
                        .or(friendship.largerMember.id.eq(memberId)));

        long total = baseQuery.fetchCount();

        List<Member> content = baseQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers)
                .fetch();

        return new PageImpl<>(content, pageable, total);
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
    public FriendDetailResp findFriendDetail(Long smallerId, Long largerId, Long friendId) {
        return queryFactory
                .select(Projections.constructor(
                        FriendDetailResp.class,
                        member.id,
                        member.nickname,
                        member.country.stringValue(),
                        member.englishLevel.stringValue(),
                        member.interests,
                        member.description,
                        member.profileImageUrl,
                        friendship.createdAt,
                        member.lastSeenAt
                ))
                .from(friendship)
                .join(member).on(member.id.eq(friendId)
                        .and(friendship.smallerMember.id.eq(smallerId))
                        .and(friendship.largerMember.id.eq(largerId)))
                .fetchOne();
    }

    @Override
    public FriendshipStateInfo findFriendshipStateInfo(Long loginId, Long memberId) {
        boolean isFriend = queryFactory
                .selectOne()
                .from(friendship)
                .where(
                        friendship.smallerMember.id.eq(loginId).and(friendship.largerMember.id.eq(memberId))
                                .or(
                                        friendship.smallerMember.id.eq(memberId)
                                                .and(friendship.largerMember.id.eq(loginId))
                                )
                )
                .fetchFirst() != null;

        boolean isFriendRequestSent = queryFactory
                .selectOne()
                .from(friendshipRequest)
                .where(
                        friendshipRequest.sender.id.eq(loginId),
                        friendshipRequest.receiver.id.eq(memberId)
                )
                .fetchFirst() != null;

        Long receivedFriendRequestId = queryFactory
                .select(friendshipRequest.id)
                .from(friendshipRequest)
                .where(
                        friendshipRequest.sender.id.eq(memberId),
                        friendshipRequest.receiver.id.eq(loginId)
                )
                .fetchOne();

        return new FriendshipStateInfo(
                isFriend,
                isFriendRequestSent,
                receivedFriendRequestId
        );
    }
}
