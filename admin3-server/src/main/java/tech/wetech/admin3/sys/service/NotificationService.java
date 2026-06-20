package tech.wetech.admin3.sys.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.wetech.admin3.common.BusinessException;
import tech.wetech.admin3.common.CommonResultStatus;
import tech.wetech.admin3.common.Constants;
import tech.wetech.admin3.common.SessionItemHolder;
import tech.wetech.admin3.sys.model.Notification;
import tech.wetech.admin3.sys.model.User;
import tech.wetech.admin3.sys.repository.NotificationRepository;
import tech.wetech.admin3.sys.repository.UserRepository;
import tech.wetech.admin3.sys.service.dto.PageDTO;
import tech.wetech.admin3.sys.service.dto.UserinfoDTO;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public PageDTO<Notification> findMyNotifications(Pageable pageable) {
        UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
        if (currentUser == null) {
            throw new BusinessException(CommonResultStatus.UNAUTHORIZED);
        }
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedTimeDesc(currentUser.userId(), pageable);
        return new PageDTO<>(page.getContent(), page.getTotalElements());
    }

    public long countUnread() {
        UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
        if (currentUser == null) return 0;
        return notificationRepository.countByUserIdAndReadFalse(currentUser.userId());
    }

    @Transactional
    public void markAllAsRead() {
        UserinfoDTO currentUser = (UserinfoDTO) SessionItemHolder.getItem(Constants.SESSION_CURRENT_USER);
        if (currentUser == null) return;
        notificationRepository.markAllAsReadByUserId(currentUser.userId());
    }

    @Transactional
    public void sendNotification(Long userId, String title, String content) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(CommonResultStatus.RECORD_NOT_EXIST, "用户不存在"));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRead(false);
        notificationRepository.save(notification);
    }
}
