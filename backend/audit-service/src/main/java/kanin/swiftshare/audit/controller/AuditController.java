package kanin.swiftshare.audit.controller;

import kanin.swiftshare.audit.model.AuditLogEntity;
import kanin.swiftshare.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuditController
 * Demonstrates Unit 5 Syllabus: Microservice REST endpoint receiving service-to-service
 * requests from Core Service and persisting audit records independently.
 */
@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    private static final Logger log = LoggerFactory.getLogger(AuditController.class);

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping("/events")
    public ResponseEntity<AuditLogEntity> recordAuditEvent(@RequestBody AuditLogEntity event) {
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            event.setEventType("GENERAL_AUDIT");
        }
        AuditLogEntity saved = auditLogRepository.save(event);
        log.info("Recorded audit event #{} [{}] from origin '{}'",
                saved.getId(), saved.getEventType(), saved.getOriginService());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/events")
    public ResponseEntity<List<AuditLogEntity>> getAllAuditEvents() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByIdDesc());
    }
}
