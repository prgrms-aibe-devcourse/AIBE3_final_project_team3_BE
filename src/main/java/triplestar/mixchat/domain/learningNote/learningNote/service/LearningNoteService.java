package triplestar.mixchat.domain.learningNote.learningNote.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningFilter;
import triplestar.mixchat.domain.learningNote.learningNote.dto.FeedbackCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.FeedbackListResp;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteListResp;
import triplestar.mixchat.domain.learningNote.learningNote.entity.Feedback;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;

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

        for (FeedbackCreateReq item : req.feedback()) {
            Feedback fb = Feedback.create(note, item.tag(), item.problem(), item.correction(), item.extra());
            note.addFeedback(fb);
        }

        return learningNoteRepository.save(note).getId();
    }

    @Transactional
    public Page<LearningNoteListResp> getLearningNotes(Pageable  pageable, Long memberId, TranslationTagCode tag, LearningFilter learningFilter) {

        Boolean isMarked = switch (learningFilter) {
            case LEARNED -> true;
            case UNLEARNED -> false;
            case ALL -> null;
        };

        Page<LearningNote> notes = learningNoteRepository.findByMemberWithFilters(memberId, tag, isMarked, pageable);

        return notes.map(note ->
                new LearningNoteListResp(
                        note.getOriginalContent(),
                        note.getCorrectedContent(),
                        note.getFeedbacks().stream()
                                .map(FeedbackListResp::from)
                                .toList()
                )
        );
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }
}