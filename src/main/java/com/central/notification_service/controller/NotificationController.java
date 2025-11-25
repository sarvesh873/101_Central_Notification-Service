package com.central.notification_service.controller;


import com.central.notification_service.service.NotificationService;
import org.openapitools.api.NotificationsApi;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
public class NotificationController implements NotificationsApi {

    @Autowired
    private NotificationService notificationService;

    @Override
    public ResponseEntity<NotificationsList> getNotificationsByUserId(String userId) {
        return notificationService.getNotificationsByUserId(userId);
    }

}
