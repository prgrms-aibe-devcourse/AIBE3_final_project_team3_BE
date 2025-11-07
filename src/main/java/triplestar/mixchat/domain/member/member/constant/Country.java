package triplestar.mixchat.domain.member.member.constant;

public enum Country {
    SOUTH_KOREA("KR"),
    UNITED_STATES("US"),
    CANADA("CA"),
    UNITED_KINGDOM("UK"),
    AUSTRALIA("AU"),
    GERMANY("DE"),
    FRANCE("FR"),
    JAPAN("JP"),
    CHINA("CN"),
    INDIA("IN");

    private final String code;

    Country(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Country findByCode(String code) {
        for (Country country : values()) {
            if (country.getCode().equalsIgnoreCase(code)) {
                return country;
            }
        }
        throw new IllegalArgumentException("해당 국가코드 입력이 잘못되었거나 등록되지 않았습니다 : " + code);
    }
}