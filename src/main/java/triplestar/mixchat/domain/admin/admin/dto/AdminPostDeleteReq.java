package triplestar.mixchat.domain.admin.admin.dto;

import jakarta.validation.constraints.NotNull;

public record AdminPostDeleteReq(
        @NotNull(message = "삭제 사유 코드는 필수입니다.")
        int reasonCode
) {}