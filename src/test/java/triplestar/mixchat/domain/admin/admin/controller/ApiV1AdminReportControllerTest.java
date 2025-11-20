package triplestar.mixchat.domain.admin.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
@DisplayName("관리자 신고 기능 테스트")
public class ApiV1AdminReportControllerTest {

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

    private Member admin;
    private Member target1;
    private Member target2;
    private Member target3;

    private Report report1;
    private Report report2;
    private Report report3;

    @BeforeEach
    void setUp() {
        admin = memberRepository.save(
                Member.createAdmin(
                        "admin@example.com",
                        Password.encrypt("AdminPass123!", passwordEncoder),
                        "관리자",
                        "admin",
                        Country.KR,
                        EnglishLevel.INTERMEDIATE,
                        List.of("관리"),
                        "관리자 계정"
                )
        );

        target1 = memberRepository.save(
                Member.createMember(
                        "target1@example.com",
                        Password.encrypt("Password1!", passwordEncoder),
                        "신고대상1",
                        "target1",
                        Country.KR,
                        EnglishLevel.BEGINNER,
                        List.of("travel"),
                        "신고 대상 유저1"
                )
        );

        target2 = memberRepository.save(
                Member.createMember(
                        "target2@example.com",
                        Password.encrypt("Password2!", passwordEncoder),
                        "신고대상2",
                        "target2",
                        Country.KR,
                        EnglishLevel.BEGINNER,
                        List.of("travel"),
                        "신고 대상 유저2"
                )
        );

        target3 = memberRepository.save(
                Member.createMember(
                        "target3@example.com",
                        Password.encrypt("Password3!", passwordEncoder),
                        "신고대상3",
                        "target3",
                        Country.KR,
                        EnglishLevel.BEGINNER,
                        List.of("travel"),
                        "신고 대상 유저3"
                )
        );

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
                        "사기 의심 메시지 2",
                        "사기 신고 2"
                )
        );

        report3 = reportRepository.save(
                Report.createWaitingReport(
                        target3.getId(),
                        ReportCategory.INAPPROPRIATE,
                        "부적절한 메시지 3",
                        "부적절 신고 3"
                )
        );
    }

    @Test
    @DisplayName("관리자 신고 목록 조회 - 페이지네이션 포함, 3건 조회")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getReports_success() throws Exception {
        mockMvc.perform(
                        get("/api/v1/admin/reports")
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("신고 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    @Test
    @DisplayName("신고 상태 변경 - APPROVED 시 대상 회원 차단 처리")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateReportStatus_approved_blocksMember() throws Exception {

        mockMvc.perform(
                        patch("/api/v1/admin/reports/{reportId}", report1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "APPROVED"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("상태 변경 완료"));

        Report updatedReport = reportRepository.findById(report1.getId())
                .orElseThrow();
        assertThat(updatedReport.getStatus()).isEqualTo(ReportStatus.APPROVED);

        Member updatedMember = memberRepository.findById(target1.getId())
                .orElseThrow();
        assertThat(updatedMember.isBlocked()).isTrue();
        assertThat(updatedMember.getBlockReason()).isEqualTo(ReportCategory.ABUSE.name());
    }

    @Test
    @DisplayName("신고 상태 변경 - REJECTED 시 대상 회원은 차단되지 않음")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateReportStatus_rejected_doesNotBlockMember() throws Exception {

        mockMvc.perform(
                        patch("/api/v1/admin/reports/{reportId}", report2.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "REJECTTED"
                                        }
                                        """.replace("REJECTTED", "REJECTED"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("상태 변경 완료"));

        Report updatedReport = reportRepository.findById(report2.getId())
                .orElseThrow();
        assertThat(updatedReport.getStatus()).isEqualTo(ReportStatus.REJECTED);

        Member updatedMember = memberRepository.findById(target2.getId())
                .orElseThrow();
        assertThat(updatedMember.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("신고 상태 변경 실패 - 관리자 권한이 아니면 403")
    @WithMockUser(username = "user", roles = "USER")
    void updateReportStatus_approved_fail_whenNotAdmin() throws Exception {

        mockMvc.perform(
                        patch("/api/v1/admin/reports/{reportId}", report3.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "APPROVED"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }
}