package triplestar.mixchat.domain.report.report.constant;

public enum ReportReason {
    ABUSE("욕설 및 비속어"),
    SCAM("사기 및 사칭"),
    INAPPROPRIATE("부적절한 언행"),
    OTHER("기타");

    private final String koLabel;
    ReportReason(String koLabel) {
        this.koLabel = koLabel;
    }
    public String ko() {
        return koLabel;
    }
}