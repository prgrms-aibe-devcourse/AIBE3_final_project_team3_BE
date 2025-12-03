package triplestar.mixchat.domain.ai.systemprompt.service;

import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

public interface TranslationProvider {
    TranslationResp translate(String originalContent);
}
