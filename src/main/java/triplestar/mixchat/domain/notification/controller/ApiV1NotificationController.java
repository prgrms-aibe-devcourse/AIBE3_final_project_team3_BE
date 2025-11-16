package triplestar.mixchat.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.notification.dto.NotificationResp;
import triplestar.mixchat.domain.notification.service.NotificationService;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class ApiV1NotificationController {

    private final NotificationService notificationService;

    // NOTE : 커서 페이지네이션은 추후에 필요하면 구현
    @GetMapping
    public CustomResponse<Page<NotificationResp>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NotificationResp> notifications = notificationService.getNotifications(userDetails.getId(), pageable);
        return CustomResponse.ok("알림 목록 조회 성공", notifications);
    }

    @PatchMapping("/read-all")
    public CustomResponse<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.markAllAsRead(userDetails.getId());
        return CustomResponse.ok("모든 알림 읽음 처리 성공");
    }

    @PatchMapping("/read/{id}")
    public CustomResponse<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(userDetails.getId(), id);
        return CustomResponse.ok("알림 읽음 처리 성공");
    }

    @DeleteMapping
    public CustomResponse<Void> deleteAllNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.deleteAllNotifications(userDetails.getId());
        return CustomResponse.ok("모든 알림 삭제 성공");
    }

    @DeleteMapping("/{id}")
    public CustomResponse<Void> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        notificationService.deleteNotification(userDetails.getId(), id);
        return CustomResponse.ok("알림 삭제 성공");
    }
}
