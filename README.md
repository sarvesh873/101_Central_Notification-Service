# Central Notification Service

A robust, production-ready notification service built with Spring Boot that handles transaction and reward notifications via multiple channels (Email, SMS, Push).

## Features

- **Transaction Notifications**: Real-time notifications for senders and receivers
- **Reward Notifications**: Special notifications for reward grants
- **Multi-channel Support**: Email, SMS, and Push notifications
- **Kafka Integration**: Event-driven architecture using Kafka
- **Asynchronous Processing**: Non-blocking notification delivery
- **User Preferences**: Configurable notification settings per user

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose (for local development)
- Kafka
- PostgreSQL
- SMTP Server (for email notifications)
- SMS Gateway (for SMS notifications)

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/sarvesh873/101_Central_Notification-Service
   cd 101_Central_Notification-Service
   ```

2. **Configure environment variables**
   Copy `.env.example` to `.env` and update the values:
   ```bash
   cp .env.example .env
   ```

3. **Start dependencies**
   ```bash
   docker-compose up -d kafka postgres
   ```

4. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## API Documentation

- **Swagger UI**: `http://localhost:8082/swagger-ui.html`
- **Actuator**: `http://localhost:8082/actuator`

## Configuration

Update `application.properties` or use environment variables:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/notification_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Email
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# SMS
sms.provider.url=https://api.smsgateway.com/send
sms.provider.api-key=your-api-key
```

## Architecture

```
┌─────────────┐     ┌─────────────────┐     ┌──────────────────┐
│   Kafka     │────▶│  Notification   │────▶│  Notification    │
│  (Events)   │     │   Consumer      │     │   Service        │
└─────────────┘     └─────────────────┘     └────────┬─────────┘
                                                     │
                                                     ▼
┌─────────────┐     ┌─────────────────┐     ┌──────────────────┐
│  Database   │◀────│  Notification   │◀────│  Email/SMS/Push  │
│ (PostgreSQL)│     │   Repository    │     │   Services       │
└─────────────┘     └─────────────────┘     └──────────────────┘
```

## Development

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Code Style
This project uses Google Java Style with the following modifications:
- 4 spaces for indentation
- 120 character line length

### Git Hooks
Pre-commit hooks are configured to ensure code quality:
- Checkstyle validation
- Test execution
- Code formatting

## Monitoring

### Health Checks
- Service health: `GET /actuator/health`
- Info: `GET /actuator/info`
- Metrics: `GET /actuator/metrics`

### Logging
Logs are written to `logs/notification-service.log` with rotation.

## Deployment

### Docker
```bash
docker build -t notification-service .
docker run -p 8080:8080 notification-service
```

### Kubernetes
See `k8s/` directory for deployment manifests.

## TODO List

### High Priority
- [ ] Update Protobuf to include transaction timestamps and user contact info
  - Add `transaction_timestamp` field
  - Add `email` and `phone_number` fields to user data
  - Add `notification_preferences` message type

- [ ] Implement actual email sending
  - Configure SMTP server
  - Create email templates
  - Handle email delivery status
  - Implement retry mechanism for failed deliveries

- [ ] Implement SMS notifications
  - Integrate with SMS gateway
  - Create SMS templates
  - Handle delivery reports

- [ ] Add user preference management
  - Email notification preferences
  - SMS notification preferences
  - Push notification preferences
  - Notification frequency settings

### Medium Priority
- [ ] Add rate limiting
- [ ] Implement circuit breakers for external services
- [ ] Add message queuing for high-volume notifications
- [ ] Implement notification batching

### Low Priority
- [ ] Add support for rich media notifications
- [ ] Implement notification templates
- [ ] Add support for in-app notifications
- [ ] Implement notification analytics

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request


## Support

For support, email support@example.com or open an issue in the GitHub repository.
