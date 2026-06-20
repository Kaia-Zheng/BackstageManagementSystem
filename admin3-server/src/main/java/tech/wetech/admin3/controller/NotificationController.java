package tech.wetech.admin3.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.wetech.admin3.sys.model.Notification;
import tech.wetech.admin3.sys.service.NotificationService;
import tech.wetech.admin3.sys.service.dto.PageDTO;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<PageDTO<Notification>> findMyNotifications(Pageable pageable) {
        return ResponseEntity.ok(notificationService.findMyNotifications(pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> countUnread() {
        return ResponseEntity.ok(notificationService.countUnread());
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
