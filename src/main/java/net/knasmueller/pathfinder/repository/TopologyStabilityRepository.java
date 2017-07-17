package net.knasmueller.pathfinder.repository;


import net.knasmueller.pathfinder.entities.TopologyStability;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopologyStabilityRepository extends CrudRepository<TopologyStability, Long> {
    @Query("SELECT ts FROM TopologyStability ts WHERE ts.topologyHash = :topologyHash order by ts.timestamp desc")
    List<TopologyStability> findAllTop20ByTopologyHashOrderByTimestamp(@Param("topologyHash") String topologyHash, Pageable page);

}
