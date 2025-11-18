package triplestar.mixchat.domain.admin.admin.service;

import com.sun.jdi.request.DuplicateRequestException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameCreateReq;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameListResp;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;
import triplestar.mixchat.domain.miniGame.sentenceGame.repository.SentenceGameRepository;

@Service
@RequiredArgsConstructor
public class AdminSentenceGameService {

    private final SentenceGameRepository sentenceGameRepository;

    @Transactional
    public Long createSentenceGame(AdminSentenceGameCreateReq request) {

        boolean duplicated = sentenceGameRepository.existsByOriginalContentAndCorrectedContent(
                request.originalContent(),
                request.correctedContent()
        );

        if (duplicated) {
            throw new DuplicateRequestException("이미 등록된 문장입니다.");
        }

        SentenceGame game = SentenceGame.createSentenceGame(
                request.originalContent(),
                request.correctedContent()
        );

        return sentenceGameRepository.save(game).getId();
    }

    @Transactional
    public List<AdminSentenceGameListResp> getList() {
        return sentenceGameRepository.findAll().stream()
                .map(AdminSentenceGameListResp::from)
                .toList();
    }
}