package triplestar.mixchat.domain.admin.constant;

import lombok.Getter;

@Getter
public enum PostDeleteReason {

    ABUSE(1, "욕설/비방"),
    HARASSMENT(2, "부적절 표현"),
    SPAM(3, "스팸/도배"),
    ILLEGAL(4, "불법/유해 콘텐츠"),
    SEXUAL(5, "음란물/청소년 유해"),
    OTHER(99, "기타");

    private final int code;
    private final String label;

    PostDeleteReason(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PostDeleteReason fromCode(int code) {
        for (PostDeleteReason r : values()) {
            if (r.code == code) return r;
        }
        return OTHER;
    }
}