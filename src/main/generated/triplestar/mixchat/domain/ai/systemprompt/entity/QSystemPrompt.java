package triplestar.mixchat.domain.ai.systemprompt.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSystemPrompt is a Querydsl query type for SystemPrompt
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSystemPrompt extends EntityPathBase<SystemPrompt> {

    private static final long serialVersionUID = 1067304183L;

    public static final QSystemPrompt systemPrompt = new QSystemPrompt("systemPrompt");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified _super = new triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath promptKey = createString("promptKey");

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QSystemPrompt(String variable) {
        super(SystemPrompt.class, forVariable(variable));
    }

    public QSystemPrompt(Path<? extends SystemPrompt> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSystemPrompt(PathMetadata metadata) {
        super(SystemPrompt.class, metadata);
    }

}

