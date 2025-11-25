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

//    public void sendSenderTransactionEvent(String key, Transaction transaction) {
//        sendToTopic(SENDER_TOPIC, key, transaction, "sender");
//    }
//
//    public void sendReceiverTransactionEvent(String key, Transaction transaction) {
//        sendToTopic(RECEIVER_TOPIC, key, transaction, "receiver");
//    }
//
//    private void sendToTopic(String topic, String key, Transaction transaction, String eventType) {
//        log.info("Sending {} event to Kafka → Topic: {}, Key: {}, Message: {}", eventType, topic, key, transaction);
//
//        TransactionEvent transactionEvent = TransactionEvent.newBuilder()
//                .setTransactionId(transaction.getTransaction_id().toString())
//                .setSenderId(transaction.getSenderId())
//                .setReceiverId(transaction.getReceiverId())
//                .setAmount(transaction.getAmount())
//                .setStatus(transaction.getStatus())
//                .setCreatedAt(Timestamp.newBuilder()
//                        .setSeconds(transaction.getInitiatedAt().toEpochSecond(ZoneOffset.UTC))
//                        .setNanos(0)
//                        .build())
//                .setUpdatedAt(Timestamp.newBuilder()
//                        .setSeconds(transaction.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
//                        .setNanos(0)
//                        .build())
//                .build();
//
//        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(topic, key, transactionEvent.toByteArray());
//
//        future.thenAccept(result -> {
//            RecordMetadata metadata = result.getRecordMetadata();
//            log.info("Kafka message sent successfully! Topic: {}, Partition: {}, Offset: {}",
//                    metadata.topic(), metadata.partition(), metadata.offset());
//        }).exceptionally(ex -> {
//            log.error("Failed to send Kafka message to topic {}: {}", topic, ex.getMessage(), ex);
//            return null;
//        });
//    }


    @KafkaListener(topics =SENDER_TOPIC, groupId = "notification-service")
    public void consumeTransaction(byte[] event) {

        try {
            TransactionEvent transactionEvent = TransactionEvent.parseFrom(event);

            // Log the received event
            log.info("Received transaction event: {}", transactionEvent);

            // Create and save notification
            Notification notification = createNotificationFromEvent(transactionEvent, "SENDER");
            notificationService.saveNotification(notification);

            log.info("Successfully processed and saved notification for transaction: {}",
                    transactionEvent.getTransactionId());

        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse transaction event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process transaction event", e);
        }
//
//        notificationRepository.save(notification);
//        System.out.println("✅ Notification saved: " + notification);
    }

}
