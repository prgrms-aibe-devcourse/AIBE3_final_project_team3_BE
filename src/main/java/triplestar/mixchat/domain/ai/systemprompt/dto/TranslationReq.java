package triplestar.mixchat.domain.ai.systemprompt.dto;

public record TranslationReq(
    String chatMessageId,
    String originalContent
) {
}
