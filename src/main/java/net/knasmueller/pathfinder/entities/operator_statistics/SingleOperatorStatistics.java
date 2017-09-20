package net.knasmueller.pathfinder.entities.operator_statistics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Container for the statistical data for one specific operator at a specific time
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class SingleOperatorStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * Timestamp of statistics measurement
     */
    Timestamp timestamp;

    String operatorName;

    int maximumCpuFrequency;
    double expectedDuration;
    double actualDuration;
    int itemsWaiting;
    double incomingRate;
    double deliveryRate;
    double networkUpload;
    double networkDownload;
    double actualCpuCores;
    int actualMemory;
    double actualStorage;

    public int getMaximumCpuFrequency() {
        return maximumCpuFrequency;
    }

    public void setMaximumCpuFrequency(int maximumCpuFrequency) {
        this.maximumCpuFrequency = maximumCpuFrequency;
    }

    public double getExpectedDuration() {
        return expectedDuration;
    }

    public void setExpectedDuration(double expectedDuration) {
        this.expectedDuration = expectedDuration;
    }

    public double getActualDuration() {
        return actualDuration;
    }

    public void setActualDuration(double actualDuration) {
        this.actualDuration = actualDuration;
    }

    public int getItemsWaiting() {
        return itemsWaiting;
    }

    public void setItemsWaiting(int itemsWaiting) {
        this.itemsWaiting = itemsWaiting;
    }

    public double getIncomingRate() {
        return incomingRate;
    }

    public void setIncomingRate(double incomingRate) {
        this.incomingRate = incomingRate;
    }

    public double getDeliveryRate() {
        return deliveryRate;
    }

    public void setDeliveryRate(double deliveryRate) {
        this.deliveryRate = deliveryRate;
    }

    public double getNetworkUpload() {
        return networkUpload;
    }

    public void setNetworkUpload(double networkUpload) {
        this.networkUpload = networkUpload;
    }

    public double getNetworkDownload() {
        return networkDownload;
    }

    public void setNetworkDownload(double networkDownload) {
        this.networkDownload = networkDownload;
    }

    public double getActualCpuCores() {
        return actualCpuCores;
    }

    public void setActualCpuCores(double actualCpuCores) {
        this.actualCpuCores = actualCpuCores;
    }

    public int getActualMemory() {
        return actualMemory;
    }

    public void setActualMemory(int actualMemory) {
        this.actualMemory = actualMemory;
    }

    public double getActualStorage() {
        return actualStorage;
    }

    public void setActualStorage(double actualStorage) {
        this.actualStorage = actualStorage;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public SingleOperatorStatistics() {
        timestamp = new Timestamp(System.currentTimeMillis());
    }



    public static SingleOperatorStatistics fromDefault(String operatorName) {
        SingleOperatorStatistics s = new SingleOperatorStatistics();
        s.setActualCpuCores(0.5);
        s.setActualDuration(2.0);
        s.setActualMemory(500);
        s.setActualStorage(300);
        s.setDeliveryRate(1.0);
        s.setExpectedDuration(1.0);
        s.setIncomingRate(1.0);
        s.setItemsWaiting(5);
        s.setMaximumCpuFrequency(2400);
        s.setNetworkDownload(31000);
        s.setNetworkUpload(31000);
        s.setOperatorName(operatorName);
        return s;
    }

    public static SingleOperatorStatistics fromDefault() {
        return fromDefault(null);
    }
}
