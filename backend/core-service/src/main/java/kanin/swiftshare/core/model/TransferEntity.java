package kanin.swiftshare.core.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TransferEntity
 * Demonstrates Unit 5 Syllabus: JPA Entity, lifecycle callback (@PrePersist),
 * and relational @ManyToOne mapping with PeerNode.
 */
@Entity
@Table(name = "core_transfers")
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "sender")
    private String sender;

    @Column(name = "receiver")
    private String receiver;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "transfer_date")
    private String transferDate;

    @Column(name = "status")
    private String status;

    @Column(name = "audit_status")
    private String auditStatus = "PENDING";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "peer_node_id")
    private PeerNode peerNode;

    public TransferEntity() {}

    public TransferEntity(String fileName, String sender, String receiver, Long sizeBytes, String status) {
        this.fileName = fileName;
        this.sender = sender;
        this.receiver = receiver;
        this.sizeBytes = sizeBytes;
        this.status = status;
    }

    @PrePersist
    public void onPrePersist() {
        if (this.transferDate == null || this.transferDate.trim().isEmpty()) {
            this.transferDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "Pending";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(String transferDate) {
        this.transferDate = transferDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public PeerNode getPeerNode() {
        return peerNode;
    }

    public void setPeerNode(PeerNode peerNode) {
        this.peerNode = peerNode;
    }
}
