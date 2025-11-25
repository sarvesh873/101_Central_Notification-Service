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

import static com.central.notification_service.utils.ServiceUtils.createNotificationFromEvent;

@Slf4j
@Component
public class KafkaNotificationsConsumer {
    private static final String SENDER_TOPIC = "txn-sender-events";
    private static final String RECEIVER_TOPIC = "txn-receiver-events";

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



    public void consumeTransaction(byte[] event, String eventType) {

        try {
            TransactionEvent transactionEvent = TransactionEvent.parseFrom(event);

            // Log the received event
            log.info("Received transaction event: {}", transactionEvent);

            // Create and save notification
            Notification notification = createNotificationFromEvent(transactionEvent, eventType);
            notificationService.saveNotification(notification);

            log.info("Successfully processed and saved notification for transaction: {}",
                    transactionEvent.getTransactionId());

        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse transaction event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process transaction event", e);
        }
    }



}
