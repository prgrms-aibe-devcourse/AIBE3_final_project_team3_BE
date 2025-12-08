package triplestar.mixchat.domain.chat.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDirectChatRoom is a Querydsl query type for DirectChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDirectChatRoom extends EntityPathBase<DirectChatRoom> {

    private static final long serialVersionUID = -881510539L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDirectChatRoom directChatRoom = new QDirectChatRoom("directChatRoom");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntity _super = new triplestar.mixchat.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> currentSequence = createNumber("currentSequence", Long.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final triplestar.mixchat.domain.member.member.entity.QMember user1;

    public final triplestar.mixchat.domain.member.member.entity.QMember user2;

    public QDirectChatRoom(String variable) {
        this(DirectChatRoom.class, forVariable(variable), INITS);
    }

    public QDirectChatRoom(Path<? extends DirectChatRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDirectChatRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDirectChatRoom(PathMetadata metadata, PathInits inits) {
        this(DirectChatRoom.class, metadata, inits);
    }

    public QDirectChatRoom(Class<? extends DirectChatRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user1 = inits.isInitialized("user1") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("user1"), inits.get("user1")) : null;
        this.user2 = inits.isInitialized("user2") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("user2"), inits.get("user2")) : null;
    }

}

