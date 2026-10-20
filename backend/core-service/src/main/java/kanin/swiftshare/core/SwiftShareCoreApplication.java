package kanin.swiftshare.core;

import kanin.swiftshare.core.model.PeerNode;
import kanin.swiftshare.core.model.TransferEntity;
import kanin.swiftshare.core.repository.PeerNodeRepository;
import kanin.swiftshare.core.repository.TransferRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * SwiftShareCoreApplication
 * Entry point for the primary Spring Boot REST API and Spring Data JPA backend.
 */
@SpringBootApplication
public class SwiftShareCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwiftShareCoreApplication.class, args);
    }

    /**
     * Seeds initial metadata to demonstrate persistent JPA operations out-of-the-box.
     */
    @Bean
    public CommandLineRunner seedInitialData(TransferRepository transferRepo, PeerNodeRepository peerRepo) {
        return args -> {
            if (transferRepo.count() == 0) {
                PeerNode localHost = peerRepo.save(new PeerNode("127.0.0.1:8080", "Local Host Peer"));
                PeerNode remoteClient = peerRepo.save(new PeerNode("127.0.0.1:52140", "Remote Client Peer"));

                TransferEntity t1 = new TransferEntity("Project_Report.pdf", localHost.getNodeAddress(), remoteClient.getNodeAddress(), 131072L, "Success");
                t1.setPeerNode(localHost);
                transferRepo.save(t1);

                TransferEntity t2 = new TransferEntity("firmware.bin", localHost.getNodeAddress(), remoteClient.getNodeAddress(), 524288L, "Success");
                t2.setPeerNode(localHost);
                transferRepo.save(t2);

                TransferEntity t3 = new TransferEntity("security_log.txt", remoteClient.getNodeAddress(), localHost.getNodeAddress(), 1024L, "Success");
                t3.setPeerNode(remoteClient);
                transferRepo.save(t3);

                System.out.println("✅ SwiftShare Core Service initialized with sample JPA transfer records.");
            }
        };
    }
}
