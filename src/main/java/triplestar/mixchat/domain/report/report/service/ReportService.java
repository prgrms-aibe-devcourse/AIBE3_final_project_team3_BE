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
        memberRepository.findById(request.getTargetMemberId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "신고 대상 회원이 존재하지 않습니다. id=" + request.getTargetMemberId()
                ));

        Report report = Report.createWaitingReport(
                request.getTargetMemberId(),
                request.getCategory(),
                request.getTargetMsgContent(),
                request.getReasonText()
        );

        return reportRepository.save(report).getId();
    }
}