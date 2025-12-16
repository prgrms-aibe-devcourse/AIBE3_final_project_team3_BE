package triplestar.mixchat.domain.chat.search.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.search.document.ChatMessageDocument;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageSearchService {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final AIChatRoomRepository aiChatRoomRepository;

    public void indexMessage(ChatMessage message) {
        try {
            String senderName = memberRepository.findById(message.getSenderId())
                    .map(Member::getNickname)
                    .orElse("Unknown");
            ChatMessageDocument document = ChatMessageDocument.fromEntity(message, senderName);
            elasticsearchTemplate.save(document);
        } catch (Exception e) {
            log.warn("채팅 메시지 색인 실패 - messageId={}, reason={}", message.getId(), e.getMessage());
        }
    }

    public void updateTranslation(String messageId, String translatedContent) {
        try {
            ChatMessageDocument existing = elasticsearchTemplate.get(messageId, ChatMessageDocument.class);
            if (existing == null) {
                log.debug("번역 업데이트 대상 문서를 찾지 못했습니다. messageId={}", messageId);
                return;
            }
            ChatMessageDocument updated = existing.withTranslation(translatedContent);
            elasticsearchTemplate.save(updated);
        } catch (Exception e) {
            log.warn("채팅 메시지 번역 색인 업데이트 실패 - messageId={}, reason={}", messageId, e.getMessage());
        }
    }

    public Page<ChatMessageDocument> search(Long memberId, ChatRoomType chatRoomType, String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.length() < 2) {
            return Page.empty(pageable);
        }

        List<Long> allowedRoomIds = resolveReadableRoomIds(memberId, chatRoomType);
        if (allowedRoomIds.isEmpty()) {
            return Page.empty(pageable);
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(m -> m.multiMatch(mm -> mm
                                .fields("content^2", "translatedContent")
                                .type(TextQueryType.PhrasePrefix)
                                .operator(Operator.And)
                                .query(kw)))
                        .filter(f -> f.term(t -> t.field("chatRoomType.keyword").value(chatRoomType.name())))
                        .filter(f -> f.terms(t -> t.field("chatRoomId").terms(ts -> ts.value(
                                allowedRoomIds.stream().map(FieldValue::of).toList()
                        ))))
                ))
                .withSort(s -> s.field(f -> f.field("sequence").order(SortOrder.Desc)))
                .withPageable(pageable)
                .build();

        SearchHits<ChatMessageDocument> searchHits = elasticsearchTemplate.search(query, ChatMessageDocument.class);
        SearchPage<ChatMessageDocument> page = SearchHitSupport.searchPageFor(searchHits, pageable);
        List<ChatMessageDocument> content = page.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    private List<Long> resolveReadableRoomIds(Long memberId, ChatRoomType chatRoomType) {
        if (memberId == null || chatRoomType == null) {
            return Collections.emptyList();
        }

        return switch (chatRoomType) {
            case DIRECT, GROUP -> chatRoomMemberRepository.findChatRoomIdsByMemberIdAndChatRoomType(memberId, chatRoomType);
            case AI -> aiChatRoomRepository.findAllByMember_Id(memberId)
                    .stream()
                    .map(AIChatRoom::getId)
                    .toList();
        };
    }
}
