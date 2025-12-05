package triplestar.mixchat.domain.ai.userprompt.constant;

/**
 * 사용자 프롬프트 유형을 정의하는 열거형
 * PRE_SCRIPTED: 사전에 작성된 프롬프트
 * CUSTOM: 사용자가 직접 작성한 프롬프트
 * PREMIUM 사용자가 CUSTOM 프롬프트를 사용할 수 있음
 */
public enum UserPromptType {
    PRE_SCRIPTED,
    CUSTOM
}