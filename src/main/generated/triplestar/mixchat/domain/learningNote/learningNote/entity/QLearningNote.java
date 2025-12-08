package triplestar.mixchat.domain.learningNote.learningNote.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLearningNote is a Querydsl query type for LearningNote
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLearningNote extends EntityPathBase<LearningNote> {

    private static final long serialVersionUID = 1296280457L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLearningNote learningNote = new QLearningNote("learningNote");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified _super = new triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified(this);

    public final StringPath correctedContent = createString("correctedContent");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final ListPath<Feedback, QFeedback> feedbacks = this.<Feedback, QFeedback>createList("feedbacks", Feedback.class, QFeedback.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final triplestar.mixchat.domain.member.member.entity.QMember member;

    public final StringPath originalContent = createString("originalContent");

    public QLearningNote(String variable) {
        this(LearningNote.class, forVariable(variable), INITS);
    }

    public QLearningNote(Path<? extends LearningNote> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLearningNote(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLearningNote(PathMetadata metadata, PathInits inits) {
        this(LearningNote.class, metadata, inits);
    }

    public QLearningNote(Class<? extends LearningNote> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new triplestar.mixchat.domain.member.member.entity.QMember(forProperty("member"), inits.get("member")) : null;
    }

}

