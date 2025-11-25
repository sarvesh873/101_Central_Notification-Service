package com.central.notification_service.utils;


import com.central.notification_service.model.Notification;
import com.central.notification_service.model.NotificationChannel;
import com.central.notification_service.model.NotificationType;
import org.openapitools.model.NotificationDTO;
import transaction.events.TransactionEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class ServiceUtils {

    // Private constructor to prevent instantiation
    private ServiceUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static NotificationDTO constructNotificationResponse(Notification notification){
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId().intValue())
                .transactionId(notification.getTransactionId())
                .userId(notification.getUserId())
                .type(NotificationDTO.TypeEnum.fromValue(notification.getType().name()))
                .subject(notification.getSubject())
                .content(notification.getContent())
                .channel(NotificationDTO.ChannelEnum.fromValue(notification.getChannel().name()))
                .sentAt(notification.getSentAt().atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .build();
    }


    public static Notification createNotificationFromEvent(TransactionEvent event, String eventType) {
        return Notification.builder()
                .transactionId(event.getTransactionId())
                .userId(event.getSenderId()) // or receiverId based on your needs
                .type(NotificationType.valueOf("TRANSACTION_" + event.getStatus()))
                .subject(String.format("Transaction %s - %s",
                        event.getTransactionId(),
                        event.getStatus()))
                .content(String.format("Transaction of amount %s %s",
                        event.getAmount(),
                        event.getStatus().equals("SUCCESS") ? "was successful" : "has failed"))
                .channel(NotificationChannel.EMAIL) // or get from event if available
                .sentAt(LocalDateTime.now())
                .build();
    }

}
