package kanin.swiftshare.core.repository;

import kanin.swiftshare.core.model.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TransferRepository
 * Demonstrates Unit 5 Syllabus: Spring Data JPA Repositories, derived queries,
 * custom JPQL, and native SQL queries.
 */
@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {

    // Derived Query Method
    List<TransferEntity> findByStatus(String status);

    // Derived Case-Insensitive Search
    List<TransferEntity> findByFileNameContainingIgnoreCase(String fileName);

    // Custom JPQL Query
    @Query("SELECT t FROM TransferEntity t WHERE t.sizeBytes >= :minBytes ORDER BY t.sizeBytes DESC")
    List<TransferEntity> findLargeTransfers(@Param("minBytes") Long minBytes);

    // Custom Native SQL Query
    @Query(value = "SELECT * FROM core_transfers WHERE status = 'Success' ORDER BY id DESC LIMIT 20", nativeQuery = true)
    List<TransferEntity> findRecentSuccessfulTransfers();
}
