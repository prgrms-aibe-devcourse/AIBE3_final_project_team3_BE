package triplestar.mixchat.domain.report.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReport is a Querydsl query type for Report
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReport extends EntityPathBase<Report> {

    private static final long serialVersionUID = 365039181L;

    public static final QReport report = new QReport("report");

    public final triplestar.mixchat.global.jpa.entity.QBaseEntity _super = new triplestar.mixchat.global.jpa.entity.QBaseEntity(this);

    public final EnumPath<triplestar.mixchat.domain.report.report.constant.ReportCategory> category = createEnum("category", triplestar.mixchat.domain.report.report.constant.ReportCategory.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath reportedMsgContent = createString("reportedMsgContent");

    public final StringPath reportedReason = createString("reportedReason");

    public final EnumPath<triplestar.mixchat.domain.report.report.constant.ReportStatus> status = createEnum("status", triplestar.mixchat.domain.report.report.constant.ReportStatus.class);

    public final NumberPath<Long> targetMemberId = createNumber("targetMemberId", Long.class);

    public QReport(String variable) {
        super(Report.class, forVariable(variable));
    }

    public QReport(Path<? extends Report> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReport(PathMetadata metadata) {
        super(Report.class, metadata);
    }

}

