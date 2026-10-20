package kanin.swiftshare.core.repository;

import kanin.swiftshare.core.model.PeerNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PeerNodeRepository
 * Spring Data JPA repository for PeerNode entities.
 */
@Repository
public interface PeerNodeRepository extends JpaRepository<PeerNode, Long> {
    Optional<PeerNode> findByNodeAddress(String nodeAddress);
}
