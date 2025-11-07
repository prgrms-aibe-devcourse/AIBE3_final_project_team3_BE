package triplestar.mixchat.domain.report.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import triplestar.mixchat.domain.report.report.constant.ReportReason;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
public class Report extends BaseEntity {

    @Column(nullable = true)
    private String targetContent;

    @Column(nullable = false)
    private Long targetMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(nullable = true)
    private String reasonText;

}