package triplestar.mixchat.domain.chat.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAIChatRoom is a Querydsl query type for AIChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAIChatRoom extends EntityPathBase<AIChatRoom> {

    private static final long serialVersionUID = 872746260L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAIChatRoom aIChatRoom = new QAIChatRoom("aIChatRoom");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntity _super = new triplestar.mixchat.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final triplestar.mixchat.domain.member.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final triplestar.mixchat.domain.ai.userprompt.entity.QUserPrompt persona;

    public final EnumPath<triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType> roomType = createEnum("roomType", triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType.class);

    public QAIChatRoom(String variable) {
        this(AIChatRoom.class, forVariable(variable), INITS);
    }

    public QAIChatRoom(Path<? extends AIChatRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAIChatRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAIChatRoom(PathMetadata metadata, PathInits inits) {
        this(AIChatRoom.class, metadata, inits);
    }

    public QAIChatRoom(Class<? extends AIChatRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("member"), inits.get("member")) : null;
        this.persona = inits.isInitialized("persona") ? new triplestar.mixchat.domain.ai.userprompt.entity.QUserPrompt(forProperty("persona"), inits.get("persona")) : null;
    }

}

