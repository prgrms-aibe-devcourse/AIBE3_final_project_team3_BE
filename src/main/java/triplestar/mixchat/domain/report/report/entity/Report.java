package triplestar.mixchat.domain.report.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.global.jpa.entity.BaseEntity;


@Entity
@Table(name = "reports")
public class Report extends BaseEntity {

    private String targetContent;

    @Column(nullable = false)
    private Long targetMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    private String reasonText;

}