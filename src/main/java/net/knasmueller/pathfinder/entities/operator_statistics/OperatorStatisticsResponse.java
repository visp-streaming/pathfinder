package net.knasmueller.pathfinder.entities.operator_statistics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Wrapper for the operator statistics response from the DSPE
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class OperatorStatisticsResponse extends LinkedHashMap<String, SingleOperatorStatistics> {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    /**
     * returns timestamp of the statistics generation
     * @return timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Creates default statistics for a set of operator IDs
     * @param operators set of operator IDs in the fictional topology
     * @return default statistics response
     */
    public static OperatorStatisticsResponse fromSetOfOperatorNamesDefault(List<String> operators) {
        OperatorStatisticsResponse result = new OperatorStatisticsResponse();
        for(String operatorName : operators) {
            result.put(operatorName, SingleOperatorStatistics.fromDefault(operatorName));
        }

        return result;
    }
}
