package net.knasmueller.pathfinder.repository;


import net.knasmueller.pathfinder.entities.TopologyStability;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface VispRuntimeIdentifierRepository extends CrudRepository<VispRuntimeIdentifier, Long> {
    List<VispRuntimeIdentifier> findAll();
    @Transactional
    Long deleteByIpAndPort(String ip, int port);
}
