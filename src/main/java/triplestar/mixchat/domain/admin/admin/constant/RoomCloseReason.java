package triplestar.mixchat.domain.admin.admin.constant;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum RoomCloseReason {
    BAD_CHATS(1, "불건전한 대화"),
    MULTIPLE_RULE_BREAK(2, "규칙 위반 다수 발생"),
    REPORT_STACK(3, "신고 누적"),
    SPAM(4, "스팸/광고 방"),
    SUSPICIOUS(5, "비정상 활동 탐지"),
    OTHER(99, "기타 사유");

    private final int code;
    private final String label;

    RoomCloseReason(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RoomCloseReason fromCode(int code) {
        return Arrays.stream(values())
                .filter(r -> r.code == code)
                .findFirst()
                .orElse(OTHER);
    }
}