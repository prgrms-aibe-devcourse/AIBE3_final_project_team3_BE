package triplestar.mixchat.domain.miniGame.sentenceGame.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSentenceGame is a Querydsl query type for SentenceGame
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSentenceGame extends EntityPathBase<SentenceGame> {

    private static final long serialVersionUID = -260437510L;

    public static final QSentenceGame sentenceGame = new QSentenceGame("sentenceGame");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified _super = new triplestar.mixchat.global.jpa.entity.QBaseEntityNoModified(this);

    public final StringPath correctedContent = createString("correctedContent");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath originalContent = createString("originalContent");

    public QSentenceGame(String variable) {
        super(SentenceGame.class, forVariable(variable));
    }

    public QSentenceGame(Path<? extends SentenceGame> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSentenceGame(PathMetadata metadata) {
        super(SentenceGame.class, metadata);
    }

}

