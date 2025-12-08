package triplestar.mixchat.domain.chat.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupChatRoom is a Querydsl query type for GroupChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupChatRoom extends EntityPathBase<GroupChatRoom> {

    private static final long serialVersionUID = -1419375431L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupChatRoom groupChatRoom = new QGroupChatRoom("groupChatRoom");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntity _super = new triplestar.mixchat.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> currentSequence = createNumber("currentSequence", Long.class);

    public final StringPath description = createString("description");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final triplestar.mixchat.domain.member.member.entity.QMember owner;

    public final StringPath password = createString("password");

    public final StringPath topic = createString("topic");

    public QGroupChatRoom(String variable) {
        this(GroupChatRoom.class, forVariable(variable), INITS);
    }

    public QGroupChatRoom(Path<? extends GroupChatRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupChatRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupChatRoom(PathMetadata metadata, PathInits inits) {
        this(GroupChatRoom.class, metadata, inits);
    }

    public QGroupChatRoom(Class<? extends GroupChatRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("owner"), inits.get("owner")) : null;
    }

}

