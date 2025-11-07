package triplestar.mixchat.domain.report.report.constant;

public enum ReportCategory {
    ABUSE("욕설 및 비속어"),
    SCAM("사기 및 사칭"),
    INAPPROPRIATE("부적절한 언행"),
    OTHER("기타");

    private final String koLabel;
    ReportCategory(String koLabel) {
        this.koLabel = koLabel;
    }
    public String getKoLabel() {
        return koLabel;
    }
}