package triplestar.mixchat.domain.report.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.domain.report.report.repository.ReportRepository;

@Service
@RequiredArgsConstructor
public class ReportAdminService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Report updateReportStatus(Long reportId, ReportStatus newStatus) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다. id=" + reportId));

        report.updateStatus(newStatus);

        if (newStatus == ReportStatus.APPROVED) {
            Member member = memberRepository.findById(report.getTargetMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("신고 대상 회원을 찾을 수 없습니다. id=" + report.getTargetMemberId()));

            member.blockByReport(report.getCategory()); // 의미 있는 도메인 메서드로 처리
        }

        return report;
    }
}
