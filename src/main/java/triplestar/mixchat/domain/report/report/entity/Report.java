package triplestar.mixchat.domain.report.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.global.jpa.entity.BaseEntity;


@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private Report(
            String reportedMsgContent,
            Long targetMemberId,
            ReportStatus status,
            ReportCategory category,
            String reportedReason
    ) {
        if (targetMemberId == null || status == null || category == null) {
            throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
        }
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
        return new Report(
                reportedMsgContent,
                targetMemberId,
                ReportStatus.WAITING,
                category,
                reportedReason
        );
    }

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }
}