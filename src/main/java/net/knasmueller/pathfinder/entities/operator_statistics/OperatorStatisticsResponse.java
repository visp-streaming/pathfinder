package net.knasmueller.pathfinder.entities.operator_statistics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

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
}
