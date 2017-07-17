package net.knasmueller.pathfinder.repository;


import net.knasmueller.pathfinder.entities.TopologyStability;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopologyStabilityRepository extends CrudRepository<TopologyStability, Long> {
    List<TopologyStability> findAllByTopologyHash(String topologyHash);
}
