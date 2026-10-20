package kanin.swiftshare.audit;

import kanin.swiftshare.audit.model.AuditLogEntity;
import kanin.swiftshare.audit.repository.AuditLogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * SwiftShareAuditApplication
 * Dedicated independent Spring Boot microservice running on port 8081.
 */
@SpringBootApplication
public class SwiftShareAuditApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwiftShareAuditApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedInitialAuditLog(AuditLogRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                AuditLogEntity initEvent = new AuditLogEntity(
                        "SERVICE_BOOTSTRAP",
                        null,
                        "SwiftShare Audit Microservice initialized on port 8081 with independent persistence.",
                        "audit-service"
                );
                repository.save(initEvent);
                System.out.println("✅ SwiftShare Audit Microservice bootstrap event recorded.");
            }
        };
    }
}
