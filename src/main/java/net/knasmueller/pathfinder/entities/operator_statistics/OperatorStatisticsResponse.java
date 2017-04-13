package net.knasmueller.pathfinder.entities.operator_statistics;


import java.util.Arrays;

public class OperatorStatisticsResponse {
    /**
     * Wrapper for the response from the DSPE
     */

    SingleOperatorStatistics[] singleOperatorStatistics;

    public SingleOperatorStatistics[] getSingleOperatorStatistics() {
        return singleOperatorStatistics;
    }

    public void setSingleOperatorStatistics(SingleOperatorStatistics[] singleOperatorStatistics) {
        this.singleOperatorStatistics = singleOperatorStatistics;
    }

    @Override
    public String toString() {
        return "OperatorStatisticsResponse{" +
                "singleOperatorStatistics=" + Arrays.toString(singleOperatorStatistics) +
                '}';
    }
}
