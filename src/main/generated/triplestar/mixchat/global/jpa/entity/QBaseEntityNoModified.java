package triplestar.mixchat.global.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseEntityNoModified is a Querydsl query type for BaseEntityNoModified
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseEntityNoModified extends EntityPathBase<BaseEntityNoModified> {

    private static final long serialVersionUID = 800908739L;

    public static final QBaseEntityNoModified baseEntityNoModified = new QBaseEntityNoModified("baseEntityNoModified");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QBaseEntityNoModified(String variable) {
        super(BaseEntityNoModified.class, forVariable(variable));
    }

    public QBaseEntityNoModified(Path<? extends BaseEntityNoModified> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseEntityNoModified(PathMetadata metadata) {
        super(BaseEntityNoModified.class, metadata);
    }

}

