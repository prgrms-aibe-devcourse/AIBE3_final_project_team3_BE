package triplestar.mixchat.domain.member.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.friend.dto.FriendshipRequestResp;
import triplestar.mixchat.domain.member.friend.service.FriendshipRequestService;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.dto.MemberDetailResp;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.dto.MemberPresenceSummaryResp;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.member.presence.service.PresenceService;
import triplestar.mixchat.testutils.RedisTestContainer;
import triplestar.mixchat.testutils.TestMemberFactory;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@DisplayName("회원 - 멤버 서비스")
class MemberServiceTest extends RedisTestContainer {

    @Autowired
    MemberService memberService;

    @Autowired
    PresenceService presenceService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FriendshipRequestService friendshipRequestService;

    Member member1;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(TestMemberFactory.createMember("test1"));
    }

    @Test
    @DisplayName("멤버 정보 수정 성공")
    void member_update_info_success() {
        assertThat(member1.getEmail()).isEqualTo("test1@example.com");
        assertThat(member1.getName()).isEqualTo("test1");
        assertThat(member1.getNickname()).isEqualTo("test1");
        assertThat(member1.getCountry()).isEqualTo(Country.CA);
        assertThat(member1.getEnglishLevel()).isEqualTo(EnglishLevel.INTERMEDIATE);
        assertThat(member1.getInterests()).containsExactlyInAnyOrder("음악");
        assertThat(member1.getDescription()).isEqualTo("테스트 회원입니다.");

        MemberInfoModifyReq dto = new MemberInfoModifyReq(
                "레제",
                Country.RU,
                "자칭 여고생",
                EnglishLevel.ADVANCED,
                List.of("독서", "운동"),
                "업데이트된 자기소개입니다."
        );

        Long id = member1.getId();
        memberService.updateInfo(id, dto);

        // db를 다시 조회하여 변경사항 검증
        Member updatedMember = memberRepository.findById(id)
                .orElseThrow(() -> new AssertionError("멤버 조회 실패"));

        assertThat(updatedMember.getName()).isEqualTo("레제");
        assertThat(updatedMember.getCountry()).isEqualTo(Country.RU);
        assertThat(updatedMember.getNickname()).isEqualTo("자칭 여고생");
        assertThat(updatedMember.getEnglishLevel()).isEqualTo(EnglishLevel.ADVANCED);
        assertThat(updatedMember.getInterests()).containsExactlyInAnyOrder("독서", "운동");
        assertThat(updatedMember.getDescription()).isEqualTo("업데이트된 자기소개입니다.");
    }

    @Test
    @DisplayName("프로필 이미지 변경 성공")
    void upload_profile_image_success() {
        MockMultipartFile testFile = new MockMultipartFile(
                "multipartFile",
                "profile.png",
                "image/png",
                "dummy image content".getBytes()
        );

        memberService.uploadProfileImage(member1.getId(), testFile);

        Member updatedMember = memberRepository.findById(member1.getId())
                .orElseThrow(() -> new AssertionError("멤버 조회 실패"));

        // minio에 UUID로 저장되므로 파일 이름이 아닌 확장자만 확인
        assertThat(updatedMember.getProfileImageUrl()).isNotEqualTo(
                "http://localhost:9000/test-bucket/default-profile.webp");
        assertThat(updatedMember.getProfileImageUrl()).endsWith(".png");
    }

    @Test
    @DisplayName("프로필 이미지 null 업로드 - 기본 이미지로 변경 성공")
    void upload_profile_image_null_success() {
        memberService.uploadProfileImage(member1.getId(), null);

        Member updatedMember = memberRepository.findById(member1.getId())
                .orElseThrow(() -> new AssertionError("멤버 조회 실패"));

        assertThat(updatedMember.getProfileImageUrl()).isEqualTo(
                "http://localhost:9000/test-bucket/default-profile.webp");
    }

    @Test
    @DisplayName("프로필 이미지 파일 크기 초과 실패")
    void upload_profile_image_file_size_exceed_fail() {
        byte[] largeFileContent = new byte[4 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "multipartFile",
                "large_image.png",
                "image/png",
                largeFileContent);
        assertThatThrownBy(() -> memberService.uploadProfileImage(member1.getId(), largeFile))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로필 이미지 파일 확장자 불가 실패")
    void upload_profile_image_file_extension_invalid_fail() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "multipartFile",
                "image.txt",
                "text/plain",
                "dummy text content".getBytes());
        assertThatThrownBy(() -> memberService.uploadProfileImage(member1.getId(), invalidFile))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("회원 상세 조회 - 비회원 조회 성공")
    void get_member_details_non_member_success() {
        MemberDetailResp resp = memberService.getMemberDetails(null, member1.getId());

        assertThat(resp.memberId()).isEqualTo(member1.getId());
        assertThat(resp.nickname()).isEqualTo(member1.getNickname());
        assertThat(resp.country()).isEqualTo(member1.getCountry().name());
        assertThat(resp.englishLevel()).isEqualTo(member1.getEnglishLevel().name());
        assertThat(resp.interests()).isEqualTo(member1.getInterests());
        assertThat(resp.description()).isEqualTo(member1.getDescription());
        assertThat(resp.profileImageUrl()).isEqualTo(member1.getProfileImageUrl());
        assertThat(resp.isFriend()).isFalse();
        assertThat(resp.isFriendRequestSent()).isFalse();
        assertThat(resp.receivedFriendRequestId()).isNull();
    }

    @Test
    @DisplayName("회원 상세 조회 - 회원 조회 성공(친구 아님, 친구 신청 대기중 아님)")
    void get_member_details_member_no_friend_no_request_success() {
        Member member2 = memberRepository.save(TestMemberFactory.createMember("test2"));

        MemberDetailResp resp = memberService.getMemberDetails(member1.getId(), member2.getId());

        assertThat(resp.isFriend()).isFalse();
        assertThat(resp.isFriendRequestSent()).isFalse();
        assertThat(resp.receivedFriendRequestId()).isNull();
    }

    @Test
    @DisplayName("회원 상세 조회 - 회원 조회 성공(친구 아님, 친구 신청 대기중)")
    void get_member_details_member_success() {
        Member member2 = memberRepository.save(TestMemberFactory.createMember("test2"));

        // member1이 member2에게 친구 신청
        friendshipRequestService.sendRequest(member1.getId(), member2.getId());

        // member1이 member2의 정보를 조회
        MemberDetailResp resp = memberService.getMemberDetails(member1.getId(), member2.getId());

        assertThat(resp.isFriend()).isFalse();
        assertThat(resp.isFriendRequestSent()).isTrue();
        assertThat(resp.receivedFriendRequestId()).isNull();
    }

    @Test
    @DisplayName("회원 상세 조회 - 회원 조회 성공(친구 신청 받은 상태)")
    void get_member_details_member_received_request_success() {
        Member member2 = memberRepository.save(TestMemberFactory.createMember("test2"));

        // member2가 member1에게 친구 신청
        friendshipRequestService.sendRequest(member2.getId(), member1.getId());

        // member1이 member2의 정보를 조회
        MemberDetailResp resp = memberService.getMemberDetails(member1.getId(), member2.getId());

        assertThat(resp.isFriend()).isFalse();
        assertThat(resp.isFriendRequestSent()).isFalse();
        assertThat(resp.receivedFriendRequestId()).isNotNull();
    }

    @Test
    @DisplayName("회원 상세 조회 - 회원 조회 성공(친구)")
    void get_member_details_member_friend_success() {
        Member member2 = memberRepository.save(TestMemberFactory.createMember("test2"));

        // member1이 member2에게 친구 신청
        FriendshipRequestResp friendshipRequestResp = friendshipRequestService
                .sendRequest(member1.getId(), member2.getId());
        Long requestId = friendshipRequestResp.id();

        // member2가 친구 신청 수락
        friendshipRequestService.processRequest(member2.getId(), requestId, true);

        // member1이 member2의 정보를 조회
        MemberDetailResp resp = memberService.getMemberDetails(member1.getId(), member2.getId());

        assertThat(resp.isFriend()).isTrue();
        assertThat(resp.isFriendRequestSent()).isFalse();
        assertThat(resp.receivedFriendRequestId()).isNull();
    }

    @Test
    @DisplayName("회원 상세 조회 - 자기 자신 조회 성공")
    void get_member_details_self_success() {
        MemberDetailResp resp = memberService.getMemberDetails(member1.getId(), member1.getId());

        assertThat(resp.isFriend()).isFalse();
        assertThat(resp.isFriendRequestSent()).isFalse();
        assertThat(resp.receivedFriendRequestId()).isNull();
    }

    @Test
    @DisplayName("회원 상세 조회 - 회원 조회 실패(존재하지 않는 회원)")
    void get_member_details_member_not_found_fail() {
        assertThatThrownBy(() -> memberService.getMemberDetails(member1.getId(), Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("회원 상세 조회 - 회원 조회 실패(조회 불가 회원)")
    void get_member_details_member_not_accessible_fail() {
        Member deletedMember = memberRepository.save(
                TestMemberFactory.createDeletedMember("deletedUser"));

        assertThatThrownBy(() -> memberService.getMemberDetails(member1.getId(), deletedMember.getId()))
                .isInstanceOf(IllegalStateException.class);

        Member blockedMember = memberRepository.save(
                TestMemberFactory.createBlockedMember("blockedUser"));

        assertThatThrownBy(() -> memberService.getMemberDetails(member1.getId(), blockedMember.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void member_delete_success() {
        Long memberId = member1.getId();

        memberService.deleteSoftly(memberId);

        Member deletedMember = memberRepository.findById(memberId)
                .orElseThrow();

        assertThat(deletedMember.isDeleted()).isTrue();
        assertThat(deletedMember.getEmail()).endsWith("@deleted.user");
        assertThat(deletedMember.getName()).isEqualTo("삭제된 회원");
        assertThat(deletedMember.getNickname()).isEqualTo("삭제된 회원");
        assertThat(deletedMember.getPassword().getPassword()).isEqualTo("DELETED_MEMBER_PASSWORD");
        assertThat(deletedMember.getProfileImageUrl()).isEqualTo(
                "http://localhost:9000/test-bucket/default-profile.webp");
        assertThat(deletedMember.getDescription()).isEqualTo("삭제된 회원입니다.");
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원")
    void member_delete_not_found_fail() {
        assertThatThrownBy(() -> memberService.deleteSoftly(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("온라인 회원 목록 조회 성공")
    void find_online_members_success() {
        // 온라인 상태인 회원 3명 생성
        Member onlineMember1 = memberRepository.save(TestMemberFactory.createMember("online1"));
        Member onlineMember2 = memberRepository.save(TestMemberFactory.createMember("online2"));
        Member onlineMember3 = memberRepository.save(TestMemberFactory.createMember("online3"));

        // 온라인 상태로 설정
        presenceService.heartbeat(onlineMember1.getId());
        presenceService.heartbeat(onlineMember2.getId());
        presenceService.heartbeat(onlineMember3.getId());

        // 오프라인 상태인 회원 2명 생성
        memberRepository.save(TestMemberFactory.createMember("offline1"));
        memberRepository.save(TestMemberFactory.createMember("offline2"));

        // 온라인 회원 목록 조회
        PageRequest pageable = PageRequest.of(0, 10);
        Page<MemberPresenceSummaryResp> onlineMembers = memberService.findOnlineMembers(-1L, pageable);

        assertThat(onlineMembers.getTotalElements()).isEqualTo(3);
        assertThat(onlineMembers.getContent())
                .extracting("id")
                .containsExactlyInAnyOrder(
                        onlineMember1.getId(),
                        onlineMember2.getId(),
                        onlineMember3.getId()
                );
    }
}