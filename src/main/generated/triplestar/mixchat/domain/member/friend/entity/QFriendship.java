package triplestar.mixchat.domain.member.friend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFriendship is a Querydsl query type for Friendship
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFriendship extends EntityPathBase<Friendship> {

    private static final long serialVersionUID = -1771806025L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFriendship friendship = new QFriendship("friendship");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified _super = new triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final triplestar.mixchat.domain.member.member.entity.QMember largerMember;

    public final triplestar.mixchat.domain.member.member.entity.QMember smallerMember;

    public QFriendship(String variable) {
        this(Friendship.class, forVariable(variable), INITS);
    }

    public QFriendship(Path<? extends Friendship> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFriendship(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFriendship(PathMetadata metadata, PathInits inits) {
        this(Friendship.class, metadata, inits);
    }

    public QFriendship(Class<? extends Friendship> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.largerMember = inits.isInitialized("largerMember") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("largerMember"), inits.get("largerMember")) : null;
        this.smallerMember = inits.isInitialized("smallerMember") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("smallerMember"), inits.get("smallerMember")) : null;
    }

}

