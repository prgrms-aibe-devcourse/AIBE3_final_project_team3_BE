package triplestar.mixchat.domain.chat.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatMember is a Querydsl query type for ChatMember
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatMember extends EntityPathBase<ChatMember> {

    private static final long serialVersionUID = -1792593749L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatMember chatMember = new QChatMember("chatMember");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntity _super = new triplestar.mixchat.global.jpa.entity.QBaseEntity(this);

    public final EnumPath<triplestar.mixchat.domain.chat.chat.constant.ChatNotificationSetting> chatNotificationSetting = createEnum("chatNotificationSetting", triplestar.mixchat.domain.chat.chat.constant.ChatNotificationSetting.class);

    public final NumberPath<Long> chatRoomId = createNumber("chatRoomId", Long.class);

    public final EnumPath<triplestar.mixchat.domain.chat.chat.constant.ChatRoomType> chatRoomType = createEnum("chatRoomType", triplestar.mixchat.domain.chat.chat.constant.ChatRoomType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DateTimePath<java.time.LocalDateTime> lastReadAt = createDateTime("lastReadAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> lastReadSequence = createNumber("lastReadSequence", Long.class);

    public final triplestar.mixchat.domain.member.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public QChatMember(String variable) {
        this(ChatMember.class, forVariable(variable), INITS);
    }

    public QChatMember(Path<? extends ChatMember> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatMember(PathMetadata metadata, PathInits inits) {
        this(ChatMember.class, metadata, inits);
    }

    public QChatMember(Class<? extends ChatMember> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("member"), inits.get("member")) : null;
    }

}

