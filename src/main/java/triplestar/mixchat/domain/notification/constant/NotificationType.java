package triplestar.mixchat.domain.notification.constant;

public enum NotificationType {
    MESSAGE("%s님이 메시지를 보냈습니다."),
    FRIEND_REQUEST("%s님이 친구 요청을 보냈습니다."),
    FRIEND_REQUEST_ACCEPT("%s님이 친구 요청을 수락했습니다."),
    FRIEND_REQUEST_REJECT("%s님이 친구 요청을 거절했습니다."),
    SYSTEM_ALERT("시스템 점검"),
    CHAT_INVITATION("%s님이 채팅방에 초대했습니다.");

    private final String template;

    NotificationType(String template) {
        this.template = template;
    }

    public String formatContent(String actorName) {
        return String.format(template, actorName);
    }
}
