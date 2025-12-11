package triplestar.mixchat.global.ai;

public enum ChatClientName {
    OPENAI("openaiChatClient"),
    OPENAI_ADVANCED("openaiRagChatClient"),
    OLLAMA("ollamaChatClient");

    private final String beanName;

    ChatClientName(String beanName) {
        this.beanName = beanName;
    }

    public String beanName() {
        return beanName;
    }
}
