package kanin.swiftshare.core.service;

import kanin.swiftshare.core.dto.AuditEventDTO;
import kanin.swiftshare.core.dto.TransferRequestDTO;
import kanin.swiftshare.core.exception.ResourceNotFoundException;
import kanin.swiftshare.core.model.PeerNode;
import kanin.swiftshare.core.model.TransferEntity;
import kanin.swiftshare.core.repository.PeerNodeRepository;
import kanin.swiftshare.core.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * TransferService
 * Demonstrates Unit 4 Syllabus: Service layer business logic, constructor-based DI,
 * Spring Data JPA persistence, and real HTTP inter-service communication (Unit 5) via RestClient.
 */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepository transferRepository;
    private final PeerNodeRepository peerNodeRepository;
    private final RestClient restClient;
    private final String auditServiceUrl;

    // Constructor-based Dependency Injection (Unit 4 Syllabus requirement)
    public TransferService(
            TransferRepository transferRepository,
            PeerNodeRepository peerNodeRepository,
            @Value("${audit.service.url:http://localhost:8081/api/audit}") String auditServiceUrl) {
        this.transferRepository = transferRepository;
        this.peerNodeRepository = peerNodeRepository;
        this.auditServiceUrl = auditServiceUrl;
        this.restClient = RestClient.builder()
                .baseUrl(auditServiceUrl)
                .build();
    }

    public List<TransferEntity> getAllTransfers() {
        return transferRepository.findAll();
    }

    public TransferEntity getTransferById(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer record with ID " + id + " not found."));
    }

    @Transactional
    public TransferEntity createTransfer(TransferRequestDTO request) {
        TransferEntity entity = new TransferEntity(
                request.getFileName(),
                request.getSender(),
                request.getReceiver(),
                request.getSizeBytes(),
                request.getStatus()
        );

        // Associate peer node if present
        if (request.getSender() != null && !request.getSender().trim().isEmpty()) {
            PeerNode peer = peerNodeRepository.findByNodeAddress(request.getSender())
                    .orElseGet(() -> peerNodeRepository.save(new PeerNode(request.getSender(), "Auto-Registered Peer")));
            entity.setPeerNode(peer);
        }

        TransferEntity saved = transferRepository.save(entity);
        log.info("Persisted transfer record #{} ({}) via JPA", saved.getId(), saved.getFileName());

        // Dispatch HTTP event to Audit Microservice (Unit 5 Service-to-Service communication)
        boolean auditDelivered = dispatchAuditEvent("TRANSFER_CREATED", saved.getId(),
                "New transfer metadata registered: " + saved.getFileName() + " (" + saved.getSizeBytes() + " bytes)");
        saved.setAuditStatus(auditDelivered ? "AUDITED_DELIVERED" : "AUDIT_SERVICE_UNAVAILABLE");
        return transferRepository.save(saved);
    }

    @Transactional
    public void deleteTransfer(Long id) {
        TransferEntity existing = getTransferById(id);
        transferRepository.delete(existing);
        log.info("Deleted transfer record #{} from JPA store", id);

        // Dispatch audit notification
        dispatchAuditEvent("TRANSFER_DELETED", id, "Transfer record removed from Core JPA store.");
    }

    /**
     * Executes real HTTP service-to-service call to the Audit Microservice.
     * Returns true if successfully delivered and persisted, false if audit service is offline/unreachable.
     */
    private boolean dispatchAuditEvent(String eventType, Long transferId, String details) {
        try {
            AuditEventDTO payload = new AuditEventDTO(eventType, transferId, details, "core-service");
            restClient.post()
                    .uri("/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully dispatched audit event [{}] to Audit Microservice at {}", eventType, auditServiceUrl);
            return true;
        } catch (Exception ex) {
            // Graceful resilience fallback: log warning without failing core persistence
            log.warn("⚠️ Audit Microservice at {} unreachable or returned error: {}", auditServiceUrl, ex.getMessage());
            return false;
        }
    }

    /**
     * Retrieves audit events from the independent Audit Microservice.
     */
    public List<AuditEventDTO> getAuditEvents() {
        try {
            return restClient.get()
                    .uri("/events")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AuditEventDTO>>() {});
        } catch (Exception ex) {
            log.warn("⚠️ Unable to fetch audit telemetry from {}: {}", auditServiceUrl, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
