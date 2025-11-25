package com.central.notification_service.service;

import com.central.notification_service.model.Notification;
import org.openapitools.model.NotificationsList;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
public interface NotificationService {

    ResponseEntity<NotificationsList> getNotificationsByUserId(String userId);


    void saveNotification(Notification notification);

    @Async
    CompletableFuture<Boolean> sendEmail(String toEmail, String subject, String content);
}
