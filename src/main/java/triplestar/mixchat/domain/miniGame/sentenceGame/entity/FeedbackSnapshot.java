package triplestar.mixchat.domain.miniGame.sentenceGame.entity;

public record FeedbackSnapshot(
        String tag,
        String problem,
        String correction,
        String extra
) {}