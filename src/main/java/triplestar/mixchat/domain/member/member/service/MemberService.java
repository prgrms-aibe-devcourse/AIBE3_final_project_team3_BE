package triplestar.mixchat.domain.member.member.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.s3.S3Uploader;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final S3Uploader s3Uploader;

    @Qualifier("defaultProfileBaseURL")
    private final String defaultProfileBaseURL;

    public void updateInfo(Long memberId, MemberInfoModifyReq req) {
        Member member = findMemberById(memberId);

        member.updateInfo(req.name(),
                Country.findByCode(req.country()),
                req.nickname(),
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

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
    }
}
