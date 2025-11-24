package triplestar.mixchat.domain.member.member.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import triplestar.mixchat.domain.member.member.dto.MemberDetailResp;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.dto.MemberPresenceSummaryResp;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.member.presence.service.PresenceService;
import triplestar.mixchat.global.s3.S3Uploader;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;
    private final PresenceService presenceService;

    @Qualifier("defaultProfileImageUrl")
    private final String defaultProfileBaseURL;
    @Qualifier("maxProfileImageSizeBytes")
    private final Long maxProfileImageSize;
    @Qualifier("allowedImageTypes")
    private final Set<String> allowedImageTypes;

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
    }

    public MemberDetailResp getMemberDetails(Long signInId, Long memberId) {
        // 비회원이 조회하는 경우
        // isFriend, isPendingRequest는 모두 false로 반환
        if (signInId == null) {
            Member member = findMemberById(memberId);
            return MemberDetailResp.forAnonymousViewer(member);
        }

        // 회원이 조회하는 경우
        // 친구 관계 및 친구 신청 상태를 함께 조회
        return memberRepository.findByIdWithFriendInfo(signInId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
    }

    // NOTE : 현재 로그인한 사용자를 제외한 모든 회원 조회 -> 추후 로그인 사용자도 포함시킬지 검토 필요
    public Page<MemberPresenceSummaryResp> findAllMembers(Long currentUserId, Pageable pageable) {
        Page<Member> members = memberRepository.findAllByIdIsNot(currentUserId, pageable);

        List<Long> ids = members.stream().map(Member::getId).toList();
        Map<Long, Boolean> onlineBulk = presenceService.isOnlineBulk(ids);
        return members.map(member -> {
            Boolean isOnline = onlineBulk.getOrDefault(member.getId(), false);
            return MemberPresenceSummaryResp.from(member, isOnline);
        });
    }

    // NOTE : 현재 로그인한 사용자를 제외한 모든 회원 조회 -> 추후 로그인 사용자도 포함시킬지 검토 필요
    public Page<MemberPresenceSummaryResp> findOnlineMembers(Long currentUserId, Pageable pageable) {
        List<Long> onlineMemberIds = presenceService.getOnlineMemberIds(pageable.getOffset(), pageable.getPageSize());
        Page<Member> members = memberRepository.findByIds(currentUserId, onlineMemberIds, pageable);

        return members.map(member -> MemberPresenceSummaryResp.from(member, true));
    }

    @Transactional
    public void updateInfo(Long memberId, MemberInfoModifyReq req) {
        Member member = findMemberById(memberId);

        member.updateInfo(req.name(),
                req.nickname(),
                req.country(),
                req.englishLevel(),
                req.interests(),
                req.description());
    }

    @Transactional
    public void uploadProfileImage(Long memberId, MultipartFile multipartFile) {
        Member member = findMemberById(memberId);

        if (multipartFile == null || multipartFile.isEmpty()) {
            member.updateProfileImageUrl(defaultProfileBaseURL);
            return;
        }

        if (multipartFile.getSize() > maxProfileImageSize) {
            throw new IllegalArgumentException("프로필 사진 최대 크기: " + (maxProfileImageSize / (1024 * 1024)) + "MB");
        }

        String contentType = multipartFile.getContentType();
        if (contentType == null || !allowedImageTypes.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않는 이미지 형식입니다.");
        }

        String url = s3Uploader.uploadFile(multipartFile, "member/profile");

        member.updateProfileImageUrl(url);
    }

    @Transactional
    public void deleteSoftly(Long memberId) {
        Member member = findMemberById(memberId);
        s3Uploader.deleteFileByUrl(member.getProfileImageUrl());

        member.deleteSoftly();
        member.updateProfileImageUrl(defaultProfileBaseURL);
    }
}
