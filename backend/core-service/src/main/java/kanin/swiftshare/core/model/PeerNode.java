package kanin.swiftshare.core.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PeerNode Entity
 * Demonstrates Unit 5 Syllabus: JPA Entity mapping and relational modeling.
 */
@Entity
@Table(name = "peer_nodes")
public class PeerNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_address", nullable = false, unique = true)
    private String nodeAddress;

    @Column(name = "friendly_name")
    private String friendlyName;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    public PeerNode() {
        this.registeredAt = LocalDateTime.now();
    }

    public PeerNode(String nodeAddress, String friendlyName) {
        this.nodeAddress = nodeAddress;
        this.friendlyName = friendlyName;
        this.registeredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
