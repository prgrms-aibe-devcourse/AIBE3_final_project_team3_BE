package triplestar.mixchat.domain.ai.systemprompt.service;

import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

public interface TranslationProvider {

    String SYSTEM_PROMPT = """
        You are a Korean-to-English translation assistant.

        TASK:
        - If the input is Korean (or mixed with Korean), translate it into natural English.
        - If the input is already English, fix obvious grammar/wording mistakes and output the improved English.
        - If the input is already perfect English, return it as-is.

        OUTPUT RULES:
        - Output ONLY the final English sentence.
        - Do NOT add explanations, labels, quotes, or JSON.
        - Just return the translated/corrected sentence itself.
        """;

    TranslationResp translate(String originalContent);
}
