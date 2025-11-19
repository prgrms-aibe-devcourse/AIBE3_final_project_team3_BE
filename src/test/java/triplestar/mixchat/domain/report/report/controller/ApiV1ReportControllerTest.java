package triplestar.mixchat.domain.report.report.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.domain.report.report.repository.ReportRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("신고 기능 테스트")
public class ApiV1ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Member reporter;
    private Member admin;
    private Member target1;
    private Member target2;
    private Member target3;

    private Report report1;
    private Report report2;
    private Report report3;

    @BeforeEach
    void setUp() {
        reporter = memberRepository.save(
                Member.createMember(
                        "user@example.com", Password.encrypt("ValidPassword123", passwordEncoder),
                        "일반유저", "일반유저",
                        Country.KR, EnglishLevel.INTERMEDIATE, List.of("테스트"), "신고하는 유저"
                )
        );
        admin = memberRepository.save(
                Member.createAdmin(
                        "admin@example.com", Password.encrypt("ValidPassword123", passwordEncoder),
                        "관리자", "admin",
                        Country.KR, EnglishLevel.INTERMEDIATE, List.of("관리"), "관리자 유저"
                )
        );
        target1 = memberRepository.save(
                Member.createMember(
                        "target1@example.com", Password.encrypt("Password1!", passwordEncoder),
                        "신고대상1", "target1",
                        Country.KR, EnglishLevel.BEGINNER, List.of("travel"), "신고 대상 유저1"
                )
        );

        target2 = memberRepository.save(
                Member.createMember(
                        "target2@example.com", Password.encrypt("Password2!", passwordEncoder),
                        "신고대상2", "target2",
                        Country.KR, EnglishLevel.BEGINNER, List.of("travel"), "신고 대상 유저2"
                )
        );

        target3 = memberRepository.save(
                Member.createMember(
                        "target3@example.com", Password.encrypt("Password3!", passwordEncoder),
                        "신고대상3", "target3",
                        Country.KR, EnglishLevel.BEGINNER, List.of("travel"), "신고 대상 유저3"
                )
        );

        // 공통으로 사용할 기본 신고 3건
        report1 = reportRepository.save(
                Report.createWaitingReport(
                        target1.getId(),
                        ReportCategory.ABUSE,
                        "욕설 메시지 1",
                        "욕설 신고 1"
                )
        );
        report2 = reportRepository.save(
                Report.createWaitingReport(
                        target2.getId(),
                        ReportCategory.SCAM,
                        "사기 의심 메시지2",
                        "사기 신고2"
                )
        );
        report3 = reportRepository.save(
                Report.createWaitingReport(
                        target3.getId(),
                        ReportCategory.INAPPROPRIATE,
                        "부적절한 메시지3",
                        "부적절 신고3"
                )
        );
    }

    @Test
    @DisplayName("신고 생성 성공 - WAITING 상태로 저장되고 응답 메시지 검증")
    @WithMockUser(username = "reporter", roles = "USER")
    void createReport_success() throws Exception {
        // ReportCreateReq 필드 규칙에 맞춘 요청 DTO
        CreateReportRequest request = new CreateReportRequest(
                target1.getId(),
                ReportCategory.ABUSE,
                "욕설이 포함된 메시지 내용입니다.",
                "욕설 사용"
        );

        mockMvc.perform(
                        post("/api/v1/reports")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("신고가 완료되었습니다"));

        assertThat(reportRepository.count()).isEqualTo(4);

        Report saved = reportRepository.findAll().stream()
                .filter(r -> "욕설이 포함된 메시지 내용입니다.".equals(r.getReportedMsgContent()))
                .findFirst()
                .orElseThrow();

        assertThat(saved.getTargetMemberId()).isEqualTo(target1.getId());
        assertThat(saved.getReportedMsgContent()).isEqualTo("욕설이 포함된 메시지 내용입니다.");
        assertThat(saved.getCategory()).isEqualTo(ReportCategory.ABUSE);
        assertThat(saved.getStatus()).isEqualTo(ReportStatus.WAITING);
        assertThat(saved.getReportedReason()).isEqualTo("욕설 사용");
    }

    @Test
    @DisplayName("신고 생성 실패 - 인증되지 않은 사용자")
    void createReport_fail_whenUnauthenticated() throws Exception {
        CreateReportRequest request = new CreateReportRequest(
                target1.getId(),
                ReportCategory.ABUSE,
                "욕설이 포함된 메시지 내용입니다.",
                "욕설 사용"
        );

        mockMvc.perform(
                        post("/api/v1/reports")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());

        assertThat(reportRepository.count()).isEqualTo(3);
    }

    // ReportCreateReq 구조에 맞춘 테스트용 DTO
    private record CreateReportRequest(
            Long targetMemberId,
            ReportCategory category,
            String reportedMsgContent,
            String reportedReason
    ) {}
}