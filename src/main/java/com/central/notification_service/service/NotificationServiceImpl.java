package com.central.notification_service.service;

import com.central.notification_service.exception.NotificationForUserDoesNotExistException;

import com.central.notification_service.model.Notification;
import com.central.notification_service.repository.NotificationRepository;
import com.central.notification_service.utils.ServiceUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.NotificationDTO;
import org.openapitools.model.NotificationsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public ResponseEntity<NotificationsList> getNotificationsByUserId(String userId) {
        List<Notification> notificationEntities = notificationRepository.findByUserId(userId);

        if (notificationEntities.isEmpty()) {
            log.info("No notifications found for user ID: {}", userId);
            throw new NotificationForUserDoesNotExistException(
                    String.format("No notifications found for user ID: %s", userId)
            );
        }

        log.debug("Found {} notifications for user ID: {}", notificationEntities.size(), userId);

        List<NotificationDTO> notifications = notificationEntities.stream()
                .map(ServiceUtils::constructNotificationResponse)
                .collect(Collectors.toList());

        NotificationsList response = new NotificationsList()
                .notifications(notifications);

        return ResponseEntity.ok(response);
    }


    @Override
    public void saveNotification(Notification notification) {
        log.info("Saving notification for userId: {}, transactionId: {}",
                notification.getUserId(), notification.getTransactionId());
        notificationRepository.save(notification);
        log.debug("Notification saved for userId: {}, transactionId: {}",
                notification.getUserId(), notification.getTransactionId());
    }

}
