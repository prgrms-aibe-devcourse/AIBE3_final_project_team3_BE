package triplestar.mixchat.domain.report.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.global.jpa.entity.BaseEntity;


@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor
public class Report extends BaseEntity {

    private String reportedMsgContent;

    @Column(nullable = false)
    private Long targetMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    private String reportedReason;

    @Builder
    public Report(String reportedMsgContent,
                  Long targetMemberId,
                  ReportStatus status,
                  ReportCategory category,
                  String reportedReason) {
        this.reportedMsgContent = reportedMsgContent;
        this.targetMemberId = targetMemberId;
        this.status = status;
        this.category = category;
        this.reportedReason = reportedReason;
    }

    public static Report createWaitingReport(
            Long targetMemberId,
            ReportCategory category,
            String reportedMsgContent,
            String reportedReason
    ) {
        return Report.builder()
                .targetMemberId(targetMemberId)
                .category(category)
                .status(ReportStatus.WAITING)
                .reportedMsgContent(reportedMsgContent)
                .reportedReason(reportedReason)
                .build();
    }

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }
}