package triplestar.mixchat.domain.member.friend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFriendshipRequest is a Querydsl query type for FriendshipRequest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFriendshipRequest extends EntityPathBase<FriendshipRequest> {

    private static final long serialVersionUID = 1254620120L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFriendshipRequest friendshipRequest = new QFriendshipRequest("friendshipRequest");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified _super = new triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final triplestar.mixchat.domain.member.member.entity.QMember receiver;

    public final triplestar.mixchat.domain.member.member.entity.QMember sender;

    public QFriendshipRequest(String variable) {
        this(FriendshipRequest.class, forVariable(variable), INITS);
    }

    public QFriendshipRequest(Path<? extends FriendshipRequest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFriendshipRequest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFriendshipRequest(PathMetadata metadata, PathInits inits) {
        this(FriendshipRequest.class, metadata, inits);
    }

    public QFriendshipRequest(Class<? extends FriendshipRequest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.receiver = inits.isInitialized("receiver") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("receiver"), inits.get("receiver")) : null;
        this.sender = inits.isInitialized("sender") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("sender"), inits.get("sender")) : null;
    }

}

