package triplestar.mixchat.domain.ai.userprompt.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserPrompt is a Querydsl query type for UserPrompt
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPrompt extends EntityPathBase<UserPrompt> {

    private static final long serialVersionUID = 1249252655L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserPrompt userPrompt = new QUserPrompt("userPrompt");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntity _super = new triplestar.mixchat.global.jpa.entity.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final triplestar.mixchat.domain.member.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final EnumPath<triplestar.mixchat.domain.ai.userprompt.constant.UserPromptType> promptType = createEnum("promptType", triplestar.mixchat.domain.ai.userprompt.constant.UserPromptType.class);

    public final EnumPath<triplestar.mixchat.domain.ai.userprompt.constant.RolePlayType> rolePlayType = createEnum("rolePlayType", triplestar.mixchat.domain.ai.userprompt.constant.RolePlayType.class);

    public final StringPath title = createString("title");

    public QUserPrompt(String variable) {
        this(UserPrompt.class, forVariable(variable), INITS);
    }

    public QUserPrompt(Path<? extends UserPrompt> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserPrompt(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserPrompt(PathMetadata metadata, PathInits inits) {
        this(UserPrompt.class, metadata, inits);
    }

    public QUserPrompt(Class<? extends UserPrompt> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("member"), inits.get("member")) : null;
    }

}

