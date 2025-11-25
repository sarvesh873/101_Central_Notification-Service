package com.central.notification_service.kafka;

import com.central.notification_service.model.Notification;
import com.central.notification_service.service.NotificationService;
import com.google.protobuf.InvalidProtocolBufferException;
import transaction.events.TransactionEvent;
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
    private static final String REWARD_TOPIC = "reward-generated-topic";

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final NotificationService notificationService;

    @Autowired
    public KafkaNotificationsConsumer(KafkaTemplate<String,  byte[]> kafkaTemplate, NotificationService notificationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
    }
    @KafkaListener(topics =SENDER_TOPIC, groupId = "notification-service")
    public void sendSenderTransactionEvent(byte[] event) {
        consumeTransaction(event, "SENDER");
    }


    @KafkaListener(topics =RECEIVER_TOPIC, groupId = "notification-service")
    public void sendReceiverTransactionEvent(byte[] event) {
        consumeTransaction(event,  "RECEIVER");
    }

    @KafkaListener(topics = REWARD_TOPIC, groupId = "notification-service")
    public void handleRewardEvent(byte[] event) {
        consumeTransaction(event, "REWARD");
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

            // Send email asynchronously with timing
            CompletableFuture.runAsync(() -> {
                long emailStart = System.currentTimeMillis();
                try {
                    log.info("Sending email for transaction: {} - Start time: {}", 
                            notification.getTransactionId(), emailStart);
                            
                    notificationService.sendEmail("dummy@example.com", notification.getSubject(), notification.getContent());
                    
                    log.info("Successfully sent email for transaction: {} - Took {} ms",
                            notification.getTransactionId(), (System.currentTimeMillis() - emailStart));
                } catch (Exception e) {
                    log.error("Failed to send email for transaction {} after {} ms. Error: {}",
                            notification.getTransactionId(), (System.currentTimeMillis() - emailStart), e.getMessage(), e);
                }
            });
            
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse transaction event after {} ms. Error: {}", 
                    (System.currentTimeMillis() - startTime), e.getMessage(), e);
            throw new RuntimeException("Failed to process transaction event", e);
        } finally {
            log.info("Completed processing for transaction: {} - Total time taken: {} ms", 
                    transactionId, (System.currentTimeMillis() - startTime));
        }
    }

}
