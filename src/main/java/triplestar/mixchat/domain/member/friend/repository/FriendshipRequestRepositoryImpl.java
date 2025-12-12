package triplestar.mixchat.domain.member.friend.repository;

import static triplestar.mixchat.domain.member.friend.entity.QFriendshipRequest.friendshipRequest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.friend.entity.FriendshipRequest;
import triplestar.mixchat.domain.member.member.entity.QMember;

@Repository
@RequiredArgsConstructor
public class FriendshipRequestRepositoryImpl implements FriendshipRequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<FriendshipRequest> findByIdWithSenderReceiver(Long id) {
        QMember sender = new QMember("sender");
        QMember receiver = new QMember("receiver");

        FriendshipRequest result = queryFactory
                .selectFrom(friendshipRequest)
                .join(friendshipRequest.sender, sender).fetchJoin()
                .join(friendshipRequest.receiver, receiver).fetchJoin()
                .where(friendshipRequest.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}