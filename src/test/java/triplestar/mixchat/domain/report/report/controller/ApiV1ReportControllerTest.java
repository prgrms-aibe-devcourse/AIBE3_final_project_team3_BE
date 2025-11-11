package triplestar.mixchat.domain.report.report.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
                Member.builder()
                        .email("user@example.com")
                        .password(Password.encrypt("ValidPassword123", passwordEncoder))
                        .name("일반유저")
                        .nickname("reporter")
                        .country(Country.SOUTH_KOREA)
                        .englishLevel(EnglishLevel.INTERMEDIATE)
                        .interest("테스트")
                        .description("신고하는 유저")
                        .build()
        );
        admin = memberRepository.save(
                Member.builder()
                        .email("admin@example.com")
                        .password(Password.encrypt("ValidPassword123", passwordEncoder))
                        .name("관리자")
                        .nickname("admin")
                        .country(Country.SOUTH_KOREA)
                        .englishLevel(EnglishLevel.INTERMEDIATE)
                        .interest("관리")
                        .description("관리자 유저")
                        .build()
        );
        target1 = memberRepository.save(
                Member.builder()
                        .email("target1@example.com")
                        .password(Password.encrypt("Password1!", passwordEncoder))
                        .name("신고대상1")
                        .nickname("target1")
                        .country(Country.SOUTH_KOREA)
                        .englishLevel(EnglishLevel.BEGINNER)
                        .interest("travel")
                        .description("신고 대상 유저1")
                        .build()
        );

        target2 = memberRepository.save(
                Member.builder()
                        .email("target2@example.com")
                        .password(Password.encrypt("Password2!", passwordEncoder))
                        .name("신고대상2")
                        .nickname("target2")
                        .country(Country.SOUTH_KOREA)
                        .englishLevel(EnglishLevel.BEGINNER)
                        .interest("travel")
                        .description("신고 대상 유저2")
                        .build()
        );

        target3 = memberRepository.save(
                Member.builder()
                        .email("target3@example.com")
                        .password(Password.encrypt("Password3!", passwordEncoder))
                        .name("신고대상3")
                        .nickname("target3")
                        .country(Country.SOUTH_KOREA)
                        .englishLevel(EnglishLevel.BEGINNER)
                        .interest("travel")
                        .description("신고 대상 유저3")
                        .build()
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
                .andExpect(jsonPath("$.msg").value("신고가 완료되었습니다"))
                .andExpect(jsonPath("$.data").doesNotExist());

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
                .andExpect(jsonPath("$.data.size").value(20))
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
                                          "status": "REJECTED"
                                        }
                                        """)
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

    // ReportCreateReq 구조에 맞춘 테스트용 DTO
    private record CreateReportRequest(
            Long targetMemberId,
            ReportCategory category,
            String reportedMsgContent,
            String reportedReason
    ) {}
}