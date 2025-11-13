package triplestar.mixchat.domain.learningNote.learningNote.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningStatus;
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

    @Transactional(readOnly = true)
    public List<LearningNoteListResp> getLearningNotes(int page, int size, TranslationTagCode tag, LearningStatus status) {
        PageRequest pageable = PageRequest.of(page, size);

        List<LearningNote> notes = learningNoteRepository.findAll(pageable).getContent();

        List<LearningNoteListResp> result = new ArrayList<>();

        for (LearningNote note : notes) {
            // 번역 태그, 학습 여부에 따른 필터링
            List<Feedback> filteredFeedbacks = note.getFeedbacks().stream()
                    .filter(f -> f.getTag() == tag)
                    .filter(f -> switch (status) {
                        case ALL -> true;
                        case LEARNED -> f.isMarked();
                        case UNLEARNED -> !f.isMarked();
                    })
                    .toList();
            // 학습노트에 담을 피드백 DTO 변환
            List<FeedbackListResp> feedbackResp = filteredFeedbacks.stream()
                    .map(f -> new FeedbackListResp(
                            f.getTag(),
                            f.getProblem(),
                            f.getCorrection(),
                            f.getExtra(),
                            f.isMarked()
                    ))
                    .toList();
            // 학습노트 DTO 구성
            result.add(new LearningNoteListResp(
                    note.getOriginalContent(),
                    note.getCorrectedContent(),
                    feedbackResp
            ));
        }
        return result;
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }
}