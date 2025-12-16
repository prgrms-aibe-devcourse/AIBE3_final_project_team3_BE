package triplestar.mixchat.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.report.dto.ReportCreateReq;
import triplestar.mixchat.domain.report.entity.Report;
import triplestar.mixchat.domain.report.repository.ReportRepository;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createReport(ReportCreateReq request) {
        memberRepository.findById(request.targetMemberId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "신고 대상 회원이 존재하지 않습니다. id=" + request.targetMemberId()
                ));

        Report report = Report.createWaitingReport(
                request.targetMemberId(),
                request.category(),
                request.reportedMsgContent(),
                request.reportedReason()
        );

        return reportRepository.save(report).getId();
    }
}