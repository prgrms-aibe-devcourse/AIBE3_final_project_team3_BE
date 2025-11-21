package triplestar.mixchat.domain.learningNote.learningNote.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningFilter;
import triplestar.mixchat.domain.learningNote.learningNote.dto.FeedbackCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteFeedbackResp;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.FeedbackRepository;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

@Service
@RequiredArgsConstructor
public class LearningNoteService {
    private final LearningNoteRepository learningNoteRepository;
    private final MemberRepository memberRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public Long createWithFeedbacks(LearningNoteCreateReq req, Long memberId) {
        Member member = findMemberById(memberId);
        LearningNote note = LearningNote.create(
                member,
                req.originalContent(),
                req.correctedContent()
        );

        for (FeedbackCreateReq item : req.feedback()) {
            Feedback fb = Feedback.create(note, item.tag(), item.problem(), item.correction(), item.extra());
            note.addFeedback(fb);
        }

        return learningNoteRepository.save(note).getId();
    }

    @Transactional
    public Page<LearningNoteFeedbackResp> getLearningNotes(Pageable  pageable, Long memberId, TranslationTagCode tag, LearningFilter learningFilter) {
        Boolean isMarked = switch (learningFilter) {
            case LEARNED -> true;
            case UNLEARNED -> false;
            case ALL -> null;
        };

        Page<Feedback> feedbacks = feedbackRepository.findFeedbacksByMember(memberId,tag,isMarked,pageable);

        return feedbacks.map(fb ->
                LearningNoteFeedbackResp.create(fb.getLearningNote(), fb)
        );
    }

    @Transactional
    public void updateFeedbackMark(Long feedbackId, Long memberId, boolean marked) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 피드백입니다."));

        if (!feedback.getLearningNote().getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("본인 피드백만 수정할 수 있습니다.");
        }

        if (marked){
            feedback.mark();
        } else{
            feedback.unmark();
        }
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }
}