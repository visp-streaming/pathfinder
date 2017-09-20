package net.knasmueller.pathfinder.entities.operator_statistics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for the operator statistics response from the DSPE
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class OperatorStatisticsResponse extends LinkedHashMap<String, SingleOperatorStatistics> {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    /**
     * returns timestamp of the statistics generation
     *
     * @return timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public OperatorStatisticsResponse(HashMap<String, HashMap<String, Object>> rawJson) {
        this.clear();
        for (String operatorId : rawJson.keySet()) {
            Map<String, Object> currentSet = rawJson.get(operatorId);
            SingleOperatorStatistics s = new SingleOperatorStatistics();
            s.setActualCpuCores((Double) ((Map<String, Object>) currentSet.get("actualResources")).get("cores"));
            s.setActualDuration((Double) currentSet.get("actualDuration"));
            s.setActualMemory((Integer) ((Map<String, Object>) currentSet.get("actualResources")).get("memory"));
            s.setActualStorage((Double) ((Map<String, Object>) currentSet.get("actualResources")).get("storage"));
            try {
                s.setDeliveryRate((Integer) currentSet.get("deliveryRate"));
            } catch(Exception e) {
                s.setDeliveryRate(0.0);
            }
            s.setExpectedDuration((Double) currentSet.get("expectedDuration"));
            s.setIncomingRate((Double) currentSet.get("incomingRate"));
            s.setItemsWaiting((Integer) currentSet.get("itemsWaiting"));
            s.setMaximumCpuFrequency((Integer) currentSet.get("frequency"));
            s.setNetworkDownload(((Double) currentSet.get("networkDownload")));
            s.setNetworkUpload((Double) currentSet.get("networkUpload"));
            s.setOperatorName(operatorId);
            this.put(operatorId, s);
        }
    }

    public OperatorStatisticsResponse() {

    }

    public static OperatorStatisticsResponse fromSetOfOperatorNamesDefault(List<String> operators) {
        OperatorStatisticsResponse result = new OperatorStatisticsResponse();
        for (String operatorName : operators) {
            result.put(operatorName, SingleOperatorStatistics.fromDefault(operatorName));
        }
        return result;
    }
}
