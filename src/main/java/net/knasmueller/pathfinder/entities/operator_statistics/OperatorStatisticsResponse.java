package net.knasmueller.pathfinder.entities.operator_statistics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OperatorStatisticsResponse extends LinkedHashMap<String, SingleOperatorStatistics> {
    /**
     * Wrapper for the response from the DSPE
     */
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public static OperatorStatisticsResponse fromSetOfOperatorNamesDefault(List<String> operators) {
        OperatorStatisticsResponse result = new OperatorStatisticsResponse();
        for(String operatorName : operators) {
            result.put(operatorName, SingleOperatorStatistics.fromDefault(operatorName));
        }

        return result;
    }
}
