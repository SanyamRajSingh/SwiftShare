package kanin.swiftshare.core.dto;

/**
 * AuditEventDTO
 * Data transfer object representing inter-service telemetry records.
 */
public class AuditEventDTO {

    private Long id;
    private String eventType;
    private Long transferId;
    private String details;
    private String originService;
    private String timestamp;

    public AuditEventDTO() {}

    public AuditEventDTO(String eventType, Long transferId, String details, String originService) {
        this.eventType = eventType;
        this.transferId = transferId;
        this.details = details;
        this.originService = originService;
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
