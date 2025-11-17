package triplestar.mixchat.domain.member.member.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.auth.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.dto.MemberProfileResp;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.s3.S3Uploader;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final S3Uploader s3Uploader;

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
    }

    public void updateInfo(Long memberId, MemberInfoModifyReq req) {
        Member member = findMemberById(memberId);

        member.updateInfo(req.name(),
                req.nickname(),
                Country.findByCode(req.country()),
                req.englishLevel(),
                req.interest(),
                req.description());

        memberRepository.save(member);
    }

    public void uploadProfileImage(Long memberId, MultipartFile multipartFile) {
        Member member = findMemberById(memberId);
        // TODO : directory 이름 상수화, 파일 사이즈 및 확장자 검증
        String url = s3Uploader.uploadFile(multipartFile, "member/profile");

        member.updateProfileImageUrl(url);
        memberRepository.save(member);
    }

    public MemberProfileResp getMemberDetails(Long signInId, Long memberId) {
        // 비회원이 조회하는 경우
        // isFriend, isPendingRequest는 모두 false로 반환
        if (signInId == null) {
            Member member = findMemberById(memberId);
            return MemberProfileResp.forAnonymousViewer(member);
        }

        // 회원이 조회하는 경우
        // 친구 관계 및 친구 신청 상태를 함께 조회
        return memberRepository.findByIdWithFriendInfo(signInId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
    }
}
