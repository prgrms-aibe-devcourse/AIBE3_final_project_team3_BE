package triplestar.mixchat.domain.admin.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.admin.admin.dto.AdminReportListResp;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.domain.report.report.repository.ReportRepository;

@Service
@RequiredArgsConstructor
public class AdminReportService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    // -- 신고 관련 --
    @Transactional
    public Report updateReportStatus(Long reportId, ReportStatus newStatus) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다. id=" + reportId));

        if (report.getStatus() != ReportStatus.WAITING) {
            throw new IllegalStateException("승인 또는 거절된 신고는 상태를 변경할 수 없습니다.");
        }

        report.updateStatus(newStatus);

        if (newStatus == ReportStatus.APPROVED) {
            Member member = memberRepository.findById(report.getTargetMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("신고 대상 회원을 찾을 수 없습니다. id=" + report.getTargetMemberId()));

            member.blockByReport(report.getCategory());
        }

        return report;
    }

    @Transactional(readOnly = true)
    public Page<AdminReportListResp> getReports(Pageable pageable) {

        Page<Report> reports = reportRepository.findAll(pageable);

        return reports.map(AdminReportListResp::from);
    }
}
