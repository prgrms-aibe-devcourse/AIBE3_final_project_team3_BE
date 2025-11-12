package triplestar.mixchat.domain.member.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.testutils.TestMemberFactory;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@DisplayName("회원 - 멤버 서비스")
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestMemberFactory.createMember("test1"));
    }

    @Test
    @DisplayName("멤버 정보 수정 성공")
    void member_update_success() {
        assertThat(member.getEmail()).isEqualTo("test1@example.com");
        assertThat(member.getName()).isEqualTo("test1");
        assertThat(member.getNickname()).isEqualTo("test1");
        assertThat(member.getCountry().getCode()).isEqualTo("CA");
        assertThat(member.getEnglishLevel()).isEqualTo(EnglishLevel.INTERMEDIATE);
        assertThat(member.getInterests()).containsExactlyInAnyOrder("음악");
        assertThat(member.getDescription()).isEqualTo("테스트 회원입니다.");

        MemberInfoModifyReq dto = new MemberInfoModifyReq(
                "레제",
                "JP",
                "자칭 여고생",
                EnglishLevel.ADVANCED,
                List.of("독서", "운동"),
                "업데이트된 자기소개입니다."
        );

        Long id = member.getId();
        memberService.updateInfo(id, dto);

        // db를 다시 조회하여 변경사항 검증
        Member updatedMember = memberRepository.findById(id)
                .orElseThrow(() -> new AssertionError("멤버 조회 실패"));

        assertThat(updatedMember.getName()).isEqualTo("레제");
        assertThat(updatedMember.getCountry().getCode()).isEqualTo("JP");
        assertThat(updatedMember.getNickname()).isEqualTo("자칭 여고생");
        assertThat(updatedMember.getEnglishLevel()).isEqualTo(EnglishLevel.ADVANCED);
        assertThat(updatedMember.getInterests()).containsExactlyInAnyOrder("독서", "운동");
        assertThat(updatedMember.getDescription()).isEqualTo("업데이트된 자기소개입니다.");
    }
}