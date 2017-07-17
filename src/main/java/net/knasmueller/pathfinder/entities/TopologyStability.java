package net.knasmueller.pathfinder.entities;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Represents the topology stability at a certain time
 */

@Entity
public class TopologyStability {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * Timestamp of statistics measurement
     */
    Timestamp timestamp;

    /**
     * identifies for which topology the stability has been computed
     */
    String topologyHash;

    double stability;

    public TopologyStability(String topologyHash, double stability) {
        this.topologyHash = topologyHash;
        this.stability = stability;
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public TopologyStability() {
    }

    public long getId() {
        return id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getTopologyHash() {
        return topologyHash;
    }

    public void setTopologyHash(String topologyHash) {
        this.topologyHash = topologyHash;
    }

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }
}
