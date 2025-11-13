package triplestar.mixchat.domain.learningNote.learningNote.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.learningNote.learningNote.dto.FeedbackItemReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class LearningNoteService {
    private final LearningNoteRepository learningNoteRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createWithFeedbacks(LearningNoteCreateReq req) {
        Member member = findMemberById(req.memberId());
        LearningNote note = LearningNote.create(
                member,
                req.originalContent(),
                req.correctedContent()
        );

        for (FeedbackItemReq item : req.feedback()) {
            Feedback fb = Feedback.create(note, item.tag(), item.problem(), item.correction(), item.extra());
            note.addFeedback(fb);
        }

        return learningNoteRepository.save(note).getId();
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }
}