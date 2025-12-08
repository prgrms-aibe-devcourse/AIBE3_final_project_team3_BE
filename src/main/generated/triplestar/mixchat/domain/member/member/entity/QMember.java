package triplestar.mixchat.domain.member.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -1345366797L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntity _super = new triplestar.mixchat.global.jpa.entity.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> blockedAt = createDateTime("blockedAt", java.time.LocalDateTime.class);

    public final StringPath blockReason = createString("blockReason");

    public final EnumPath<triplestar.mixchat.domain.member.member.constant.Country> country = createEnum("country", triplestar.mixchat.domain.member.member.constant.Country.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final StringPath email = createString("email");

    public final EnumPath<triplestar.mixchat.domain.member.member.constant.EnglishLevel> englishLevel = createEnum("englishLevel", triplestar.mixchat.domain.member.member.constant.EnglishLevel.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ListPath<String, StringPath> interests = this.<String, StringPath>createList("interests", String.class, StringPath.class, PathInits.DIRECT2);

    public final BooleanPath isBlocked = createBoolean("isBlocked");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final DateTimePath<java.time.LocalDateTime> lastSeenAt = createDateTime("lastSeenAt", java.time.LocalDateTime.class);

    public final EnumPath<triplestar.mixchat.domain.member.member.constant.MembershipGrade> membershipGrade = createEnum("membershipGrade", triplestar.mixchat.domain.member.member.constant.MembershipGrade.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final QPassword password;

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<triplestar.mixchat.domain.member.member.constant.Role> role = createEnum("role", triplestar.mixchat.domain.member.member.constant.Role.class);

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.password = inits.isInitialized("password") ? new QPassword(forProperty("password")) : null;
    }

}

