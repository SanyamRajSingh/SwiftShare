package kanin.swiftshare.audit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuditLogEntity
 * Demonstrates Unit 5 Syllabus: Independent microservice persistence model.
 */
@Entity
@Table(name = "audit_events")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "origin_service")
    private String originService;

    @Column(name = "timestamp")
    private String timestamp;

    public AuditLogEntity() {}

    public AuditLogEntity(String eventType, Long transferId, String details, String originService) {
        this.eventType = eventType;
        this.transferId = transferId;
        this.details = details;
        this.originService = originService;
    }

    @PrePersist
    public void onPrePersist() {
        if (this.timestamp == null || this.timestamp.trim().isEmpty()) {
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getOriginService() {
        return originService;
    }

    public void setOriginService(String originService) {
        this.originService = originService;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
