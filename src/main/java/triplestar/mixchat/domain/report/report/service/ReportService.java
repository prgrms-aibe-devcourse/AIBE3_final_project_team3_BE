package triplestar.mixchat.domain.report.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.report.report.dto.ReportCreateRequest;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.domain.report.report.repository.ReportRepository;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createReport(ReportCreateRequest request) {
        Report report = Report.createWaitingReport(
                request.getTargetMemberId(),
                request.getCategory(),
                request.getTargetMsgContent(),
                request.getReasonText()
        );

        Report saved = reportRepository.save(report);
        return saved.getId();
    }
}