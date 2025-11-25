package com.central.notification_service.utils;


import com.central.notification_service.model.Notification;
import com.central.notification_service.model.NotificationChannel;
import com.central.notification_service.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.NotificationDTO;
import transaction.events.TransactionEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ServiceUtils {

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

    /**
     * Creates and returns a notification based on the transaction event and type.
     * 
     * @param event The transaction event
     * @param eventType The type of event (SENDER, RECEIVER, or REWARD)
     * @return The created notification
     */
    public static Notification createNotificationFromEvent(TransactionEvent event, String eventType) {
        String userId;
        String subject;
        String content;
        NotificationType notificationType;
        
        switch (eventType) {
            case "SENDER":
                userId = event.getSenderId();
                notificationType = NotificationType.TRANSACTION_SUCCESS;
                subject = String.format("Transaction Processed: $%.2f Sent", event.getAmount());
                content = String.format(
                    "Dear Valued Customer,  " +
                    "We have successfully processed your transaction.  " +
                    "Transaction Details: " +
                    "- Amount: $%.2f " +
                    "- Recipient: %s " +
                    "- Transaction ID: %s " +
                    "- Date: %s  " +
                    "Your current account balance is $%.2f.  " +
                    "Thank you for choosing our service.  " +
                    "Best regards, The Payment Team",
                    event.getAmount(),
                    event.getReceiverId(),
                    event.getTransactionId(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    1250.00 // Hardcoded balance as per requirements
                );
                break;
                
            case "RECEIVER":
                userId = event.getReceiverId();
                notificationType = NotificationType.TRANSACTION_SUCCESS;
                subject = String.format("Payment Received: $%.2f Credited to Your Account", event.getAmount());
                content = String.format(
                    "Dear Valued Customer,  " +
                    "We are pleased to inform you that a payment has been credited to your account.  " +
                    "Transaction Details: " +
                    "- Amount: $%.2f " +
                    "- Sender: %s " +
                    "- Transaction ID: %s " +
                    "- Date: %s  " +
                    "Your current account balance is $%.2f.  " +
                    "Thank you for being a valued customer.  " +
                    "Best regards, The Payment Team",
                    event.getAmount(),
                    event.getSenderId(),
                    event.getTransactionId(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    1750.00 // Hardcoded balance as per requirements
                );
                break;
                
            case "REWARD":
                userId = event.getReceiverId();
                notificationType = NotificationType.REWARD_GRANTED;
                subject = "Congratulations on Your Reward!";
                content = String.format(
                    "Dear Valued Customer, " +
                    "We are delighted to inform you that you have been awarded a special reward! " +
                    "Reward Details: " +
                    "- Amount: $%.2f " +
                    "- Transaction ID: %s " +
                    "- Date: %s " +
                    "This reward is our way of showing appreciation for your continued trust in our services. " +
                    "The reward has been credited to your account. " +
                    "Thank you for being a valued customer. " +
                    "Best regards, The Rewards Team",
                    event.getAmount(),
                    event.getTransactionId(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
        
        return Notification.builder()
                .transactionId(event.getTransactionId())
                .userId(userId)
                .type(notificationType)
                .subject(subject)
                .content(content)
                .channel(NotificationChannel.EMAIL)
                .sentAt(LocalDateTime.now())
                .build();
    }

}
