package triplestar.mixchat.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import triplestar.mixchat.domain.notification.dto.NotificationResp;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1NotificationController", description = "API 알림(Notification) 관리 컨트롤러")
@SuccessResponse
@CommonBadResponse
@SignInInRequireResponse
@SecurityRequirement(name = "Authorization")
public interface ApiNotificationController {

    // --- 1. 알림 목록 조회 (GET /) ---
    @Operation(summary = "알림 목록 조회", description = "인증된 사용자의 알림 목록을 최신순으로 페이지네이션하여 조회합니다.")
    CustomResponse<Page<NotificationResp>> getNotifications(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            @Parameter(description = "페이지네이션 정보 (size=20, sort=createdAt, direction=DESC 기본값)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );

    // --- 2. 모든 알림 읽음 처리 (PATCH /read-all) ---
    @Operation(summary = "모든 알림 읽음 처리", description = "인증된 사용자의 모든 읽지 않은 알림을 읽음 처리합니다.")
    CustomResponse<Void> markAllAsRead(
            @Parameter(hidden = true)
            CustomUserDetails userDetails
    );

    // --- 3. 특정 알림 읽음 처리 (PATCH /read/{id}) ---
    @Operation(summary = "특정 알림 읽음 처리", description = "특정 알림 ID를 읽음 처리합니다.")
    CustomResponse<Void> markAsRead(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            @Parameter(description = "읽음 처리할 알림의 ID", example = "10")
            Long id
    );

    // --- 4. 모든 알림 삭제 (DELETE /) ---
    @Operation(summary = "모든 알림 삭제", description = "인증된 사용자의 모든 알림을 삭제합니다.")
    CustomResponse<Void> deleteAllNotification(
            @Parameter(hidden = true)
            CustomUserDetails userDetails
    );

    // --- 5. 특정 알림 삭제 (DELETE /{id}) ---
    @Operation(summary = "특정 알림 삭제", description = "특정 알림 ID를 삭제합니다.")
    CustomResponse<Void> deleteNotification(
            @Parameter(hidden = true)
            CustomUserDetails userDetails,
            @Parameter(description = "삭제할 알림의 ID", example = "10")
            Long id
    );
}