package net.knasmueller.pathfinder.entities.operator_statistics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OperatorStatisticsResponse extends LinkedHashMap<String, SingleOperatorStatistics> {
    /**
     * Wrapper for the response from the DSPE
     */


}
