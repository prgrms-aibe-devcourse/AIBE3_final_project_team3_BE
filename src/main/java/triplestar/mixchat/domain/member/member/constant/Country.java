package triplestar.mixchat.domain.member.member.constant;

public enum Country {
    SOUTH_KOREA("KR", "South Korea"),
    UNITED_STATES("US", "United States"),
    CANADA("CA", "Canada"),
    UNITED_KINGDOM("UK", "United Kingdom"),
    AUSTRALIA("AU", "Australia"),
    GERMANY("DE", "Germany"),
    FRANCE("FR", "France"),
    JAPAN("JP", "Japan"),
    CHINA("CN", "China"),
    INDIA("IN", "India");

    private final String code;
    private final String name;

    Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
