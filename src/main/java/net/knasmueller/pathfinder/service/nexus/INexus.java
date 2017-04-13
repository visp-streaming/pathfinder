package net.knasmueller.pathfinder.service.nexus;


import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;

public interface INexus {
    enum OperatorClassification {WORKING, FAILED}
    OperatorClassification predict(SingleOperatorStatistics singleOperatorStatistics);
}
