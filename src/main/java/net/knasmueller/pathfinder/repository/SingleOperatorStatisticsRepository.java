package net.knasmueller.pathfinder.repository;


import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SingleOperatorStatisticsRepository extends CrudRepository<SingleOperatorStatistics, Long> {
    List<SingleOperatorStatistics> findByOperatorName(String operatorName);
}
