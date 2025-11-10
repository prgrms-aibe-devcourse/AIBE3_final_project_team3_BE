package triplestar.mixchat.domain.report.report.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Test
    @DisplayName("신고 생성 성공 - WAITING 상태로 저장되고 응답 메시지 검증")
    void createReport_success() throws Exception {
        // given: 신고 당하는 대상 멤버 생성
        Member targetMember = Member.builder()
                .email("target@example.com")
                .password(Password.encrypt("passwordD1", passwordEncoder))
                .name("신고대상")
                .nickname("targetUser")
                .country(Country.SOUTH_KOREA)
                .englishLevel(EnglishLevel.BEGINNER)
                .interest("travel")
                .description("테스트용 신고 대상 유저")
                .build();
        memberRepository.save(targetMember);

        // 요청 바디 생성
        CreateReportRequest request = new CreateReportRequest(
                targetMember.getId(),
                "욕설이 포함된 메시지 내용입니다.",
                ReportCategory.ABUSE,
                "욕설 사용"
        );

        // when & then: API 호출 및 응답 검증
        mockMvc.perform(
                post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("신고가 완료되었습니다"))
                .andExpect(jsonPath("$.data", nullValue())); // data 는 null 이어야 함

        // DB에 Report가 제대로 저장되었는지 추가 검증
        assertThat(reportRepository.count()).isEqualTo(1);

        Report saved = reportRepository.findAll().get(0);
        assertThat(saved.getTargetMemberId()).isEqualTo(targetMember.getId());
        assertThat(saved.getTargetMsgContent()).isEqualTo("욕설이 포함된 메시지 내용입니다.");
        assertThat(saved.getCategory()).isEqualTo(ReportCategory.ABUSE);
        assertThat(saved.getStatus()).isEqualTo(ReportStatus.WAITING);
        assertThat(saved.getReasonText()).isEqualTo("욕설 사용");
    }

    @Test
    @DisplayName("관리자 신고 목록 조회 - 페이지네이션 포함, 3건 조회")
    void getReports_success() throws Exception {
        Member targetMember1 = Member.builder()
                .email("target1@example.com")
                .password(Password.encrypt("Password1", passwordEncoder))
                .name("target-user1")
                .nickname("target-nick1")
                .country(Country.SOUTH_KOREA)
                .englishLevel(EnglishLevel.BEGINNER)
                .interest("travel")
                .description("신고 대상 유저1")
                .build();
        memberRepository.save(targetMember1);
        Member targetMember2 = Member.builder()
                .email("target2@example.com")
                .password(Password.encrypt("Password2", passwordEncoder))
                .name("target-user2")
                .nickname("target-nick2")
                .country(Country.SOUTH_KOREA)
                .englishLevel(EnglishLevel.BEGINNER)
                .interest("travel")
                .description("신고 대상 유저2")
                .build();
        memberRepository.save(targetMember2);
        Member targetMember3 = Member.builder()
                .email("target3@example.com")
                .password(Password.encrypt("Password3", passwordEncoder))
                .name("target-user3")
                .nickname("target-nick")
                .country(Country.SOUTH_KOREA)
                .englishLevel(EnglishLevel.BEGINNER)
                .interest("travel")
                .description("신고 대상 유저")
                .build();
        memberRepository.save(targetMember3);

        Long targetId1 = targetMember1.getId();
        Long targetId2 = targetMember2.getId();
        Long targetId3 = targetMember3.getId();

        // 신고 3건 생성 (WAITING 상태로)
        Report r1 = Report.createWaitingReport(
                targetId1,
                ReportCategory.ABUSE,
                "욕설 메시지 1",
                "욕설 신고 1"
        );
        Report r2 = Report.createWaitingReport(
                targetId2,
                ReportCategory.SCAM,
                "사기 의심 메시지2",
                "사기 신고2"
        );
        Report r3 = Report.createWaitingReport(
                targetId3,
                ReportCategory.INAPPROPRIATE,
                "부적절한 메시지3",
                "부적절 신고3"
        );
        reportRepository.save(r1);
        reportRepository.save(r2);
        reportRepository.save(r3);

        // when & then
        mockMvc.perform(
                        get("/api/v1/reports/list")  // 컨트롤러가 @RequestMapping("/api/v1/reports") + @GetMapping("/list") 인 경우
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("신고 목록 조회 성공"))

                // content 크기 (3건)
                .andExpect(jsonPath("$.data.content.length()").value(3))

                // 첫번째 요소 기본 필드 검증 (id/targetMemberId/status/category 등)
                .andExpect(jsonPath("$.data.content[0].targetMemberId").value(targetId1))
                .andExpect(jsonPath("$.data.content[0].targetMsgContent").value("욕설 메시지 1"))
                .andExpect(jsonPath("$.data.content[0].status").value("WAITING"))
                .andExpect(jsonPath("$.data.content[0].category").value(ReportCategory.ABUSE.name()))
                .andExpect(jsonPath("$.data.content[0].reasonText").value("욕설 신고 1"))
                .andExpect(jsonPath("$.data.content[1].targetMemberId").value(targetId2))
                .andExpect(jsonPath("$.data.content[1].targetMsgContent").value("사기 의심 메시지2"))
                .andExpect(jsonPath("$.data.content[1].status").value("WAITING"))
                .andExpect(jsonPath("$.data.content[1].category").value(ReportCategory.SCAM.name()))
                .andExpect(jsonPath("$.data.content[1].reasonText").value("사기 신고2"))
                .andExpect(jsonPath("$.data.content[2].targetMemberId").value(targetId3))
                .andExpect(jsonPath("$.data.content[2].targetMsgContent").value("부적절한 메시지3"))
                .andExpect(jsonPath("$.data.content[2].status").value("WAITING"))
                .andExpect(jsonPath("$.data.content[2].category").value(ReportCategory.INAPPROPRIATE.name()))
                .andExpect(jsonPath("$.data.content[2].reasonText").value("부적절 신고3"))



                // 페이지 정보(Page 객체 기본 필드 기준)
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    private record CreateReportRequest(
            Long targetMemberId,
            String targetMsgContent,
            ReportCategory category,
            String reasonText
    ) {}
}
