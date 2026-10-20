package kanin.swiftshare.core.controller;

import jakarta.validation.Valid;
import kanin.swiftshare.core.dto.AuditEventDTO;
import kanin.swiftshare.core.dto.TransferRequestDTO;
import kanin.swiftshare.core.model.TransferEntity;
import kanin.swiftshare.core.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TransferController
 * Demonstrates Unit 4 Syllabus: REST API endpoints, HTTP status codes (200, 201, 204),
 * request validation (@Valid), and cross-origin access.
 */
@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
public class TransferController {

    private final TransferService transferService;

    // Constructor Injection
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public ResponseEntity<List<TransferEntity>> getAllTransfers() {
        return ResponseEntity.ok(transferService.getAllTransfers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferEntity> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    @PostMapping
    public ResponseEntity<TransferEntity> createTransfer(@Valid @RequestBody TransferRequestDTO request) {
        TransferEntity created = transferService.createTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Audit-Status", created.getAuditStatus())
                .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransfer(@PathVariable Long id) {
        transferService.deleteTransfer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditEventDTO>> getAuditEvents() {
        return ResponseEntity.ok(transferService.getAuditEvents());
    }
}
