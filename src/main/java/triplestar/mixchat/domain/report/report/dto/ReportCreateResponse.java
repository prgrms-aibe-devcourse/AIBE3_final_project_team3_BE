package triplestar.mixchat.domain.report.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportCreateResponse {
    private String msg;
    private Data data;

    @Getter
    @AllArgsConstructor
    public static class Data {
        private Long id;
    }

    public static ReportCreateResponse of(Long reportId) {
        return new ReportCreateResponse("reported", new Data(reportId));
    }
}