package com.central.notification_service.kafka;

import com.central.notification_service.model.Notification;
import com.central.notification_service.service.NotificationService;
import com.google.protobuf.InvalidProtocolBufferException;
import notification.events.TransactionEvent;
import notification.events.RewardEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.central.notification_service.utils.ServiceUtils.createNotificationFromEvent;

@Slf4j
@Component
public class KafkaNotificationsConsumer {
    private static final String SENDER_TOPIC = "txn-sender-events";
    private static final String RECEIVER_TOPIC = "txn-receiver-events";
    private static final String REWARD_TOPIC = "reward-generated-events";

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final NotificationService notificationService;

    @Autowired
    public KafkaNotificationsConsumer(KafkaTemplate<String,  byte[]> kafkaTemplate, NotificationService notificationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
    }
    @KafkaListener(topics = SENDER_TOPIC, groupId = "notification-service")
    public void handleSenderTransaction(byte[] event) {
        consumeTransaction(event, "SENDER");
    }

    @KafkaListener(topics = RECEIVER_TOPIC, groupId = "notification-service")
    public void handleReceiverTransaction(byte[] event) {
        consumeTransaction(event, "RECEIVER");
    }

    @KafkaListener(topics = REWARD_TOPIC, groupId = "notification-service")
    public void handleRewardEvent(byte[] event) {
        consumeRewardEvent(event);
    }

    public void consumeTransaction(byte[] event, String eventType) {
        long startTime = System.currentTimeMillis();
        String transactionId = "";
        
        try {
            log.info("Successfully received {} notification for transaction: {} - Took {} ms",
                    eventType.toLowerCase(), transactionId, System.currentTimeMillis());
            
            TransactionEvent transactionEvent = TransactionEvent.parseFrom(event);
            transactionId = transactionEvent.getTransactionId();
            log.info("Processing {} event - Transaction ID: {} - Event data: {}", 
                    eventType.toLowerCase(), transactionId, transactionEvent);
            
            // Process and save notification
            long processStart = System.currentTimeMillis();
            Notification notification = createNotificationFromEvent(transactionEvent, eventType);
            notificationService.saveNotification(notification);
            log.info("Successfully processed and saved {} notification for transaction: {} - Took {} ms", 
                   eventType.toLowerCase(), transactionId, (System.currentTimeMillis() - processStart));

            // Send both email and SMS for transaction events
            sendEmailNotifications(notification);
            sendSmsNotifications(notification);
            
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse transaction event after {} ms. Error: {}", 
                    (System.currentTimeMillis() - startTime), e.getMessage(), e);
            throw new RuntimeException("Failed to process transaction event", e);
        } finally {
            log.info("Completed processing for transaction: {} - Total time taken: {} ms", 
                    transactionId, (System.currentTimeMillis() - startTime));
        }
    }
    
    /**
     * Processes reward events by sending push notifications
     * @param event The raw Kafka message containing the reward event
     */
    public void consumeRewardEvent(byte[] event) {
        long startTime = System.currentTimeMillis();
        String transactionId = "";
        
        try {
            log.info("Processing reward event");

            RewardEvent rewardEvent = RewardEvent.parseFrom(event);
            transactionId = rewardEvent.getTransactionId();
            log.info("Processing REWARD event - Transaction ID: {} - Event data: {}", 
                    transactionId, rewardEvent);
            
            // Process and save notification
            long processStart = System.currentTimeMillis();
            Notification notification = createNotificationFromEvent(rewardEvent, "REWARD");
            notificationService.saveNotification(notification);
            log.info("Successfully processed and saved REWARD notification for transaction: {} - Took {} ms", 
                    transactionId, (System.currentTimeMillis() - processStart));
            
            // Send push notification for reward
            sendPushNotification(notification);
            
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse reward event after {} ms. Error: {}", 
                    (System.currentTimeMillis() - startTime), e.getMessage(), e);
            throw new RuntimeException("Failed to process reward event", e);
        } finally {
            log.info("Completed processing for reward: {} - Total time taken: {} ms", 
                    transactionId, (System.currentTimeMillis() - startTime));
        }
    }
    
    /**
     * Sends transaction notifications (email and SMS)
     * @param notification The notification to send
     */
    private void sendEmailNotifications(Notification notification) {
        // Send email asynchronously with timing
        CompletableFuture.runAsync(() -> {
            long emailStart = System.currentTimeMillis();
            try {
                log.info("Sending email for transaction: {} - Start time: {}", 
                        notification.getTransactionId(), emailStart);
                        
                notificationService.sendEmail(
                    notification.getUserId() + "@example.com",
                    notification.getSubject(), 
                    notification.getContent()
                );
                
                log.info("Successfully sent email for transaction: {} - Took {} ms",
                        notification.getTransactionId(), (System.currentTimeMillis() - emailStart));
            } catch (Exception e) {
                log.error("Failed to send email for transaction {} after {} ms. Error: {}",
                        notification.getTransactionId(), (System.currentTimeMillis() - emailStart), e.getMessage(), e);
            }
        });
    }

    private void sendSmsNotifications(Notification notification) {
        // Send SMS asynchronously with timing
        CompletableFuture.runAsync(() -> {
            long smsStart = System.currentTimeMillis();
            try {
                log.info("Sending SMS for transaction: {} - Start time: {}",
                        notification.getTransactionId(), smsStart);

                // For demo, using a dummy phone number based on userId
                String phoneNumber = "+1" + notification.getUserId().hashCode() % 1000000000;
                notificationService.sendSms(phoneNumber,
                    notification.getSubject() + " - " +
                    notification.getContent().substring(0, Math.min(100, notification.getContent().length())));

                log.info("Successfully sent SMS for transaction: {} - Took {} ms",
                        notification.getTransactionId(), (System.currentTimeMillis() - smsStart));
            } catch (Exception e) {
                log.error("Failed to send SMS for transaction {} after {} ms. Error: {}",
                        notification.getTransactionId(), (System.currentTimeMillis() - smsStart), e.getMessage(), e);
            }
        });
    }
    
    /**
     * Sends a push notification for rewards
     * @param notification The reward notification to send
     */
    private void sendPushNotification(Notification notification) {
        CompletableFuture.runAsync(() -> {
            long pushStart = System.currentTimeMillis();
            try {
                log.info("Sending push notification for reward: {} - Start time: {}", 
                        notification.getTransactionId(), pushStart);
                        
                notificationService.sendPushNotification(
                    notification.getUserId(),
                    notification.getSubject(),
                    notification.getContent()
                );
                
                log.info("Successfully sent push notification for reward: {} - Took {} ms",
                        notification.getTransactionId(), (System.currentTimeMillis() - pushStart));
            } catch (Exception e) {
                log.error("Failed to send push notification for reward {} after {} ms. Error: {}",
                        notification.getTransactionId(), (System.currentTimeMillis() - pushStart), e.getMessage(), e);
            }
        });
    }

}
