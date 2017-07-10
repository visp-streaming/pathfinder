package net.knasmueller.pathfinder.service.nexus;


import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;

/**
 *  A classifier of operators that uses statistical parameters to decide wheter an operator is believed
 *  to be working or whether it has failed.
 */

public interface INexus {

    /**
     * The two states that represent the classification outcome
     */
    enum OperatorClassification {WORKING, FAILED}

    /**
     * Runs the classifier and produces a classification result
     * @param singleOperatorStatistics The set of statistical parameters that have been collected for the operator
     *                                 and that are used for the classification
     * @return one of the two possible classification outcomes (WORKING or FAILED)
     */
    OperatorClassification predict(SingleOperatorStatistics singleOperatorStatistics);
}
