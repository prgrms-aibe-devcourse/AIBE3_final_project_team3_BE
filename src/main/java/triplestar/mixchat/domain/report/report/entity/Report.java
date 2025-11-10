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

    private String targetMsgContent;

    @Column(nullable = false)
    private Long targetMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    private String reasonText;

    @Builder
    public Report(String targetMsgContent,
                  Long targetMemberId,
                  ReportStatus status,
                  ReportCategory category,
                  String reasonText) {
        this.targetMsgContent = targetMsgContent;
        this.targetMemberId = targetMemberId;
        this.status = status;
        this.category = category;
        this.reasonText = reasonText;
    }

    public static Report createWaitingReport(
            Long targetMemberId,
            ReportCategory category,
            String targetMsgContent,
            String reasonText
    ) {
        return Report.builder()
                .targetMemberId(targetMemberId)
                .category(category)
                .status(ReportStatus.WAITING)
                .targetMsgContent(targetMsgContent)
                .reasonText(reasonText)
                .build();
    }

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }
}