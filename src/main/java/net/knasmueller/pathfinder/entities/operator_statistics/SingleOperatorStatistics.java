package net.knasmueller.pathfinder.entities.operator_statistics;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleOperatorStatistics {

    double cpu_now, cpu_10, cpu_20, ram_now, ram_10, ram_20, network_in, network_out, round_trip, rate_source_consumption_now,
            rate_source_consumption_10, rate_source_consumption_20, rate_sent_now, rate_sent_10, rate_sent_20;
    boolean killed_process;
    int sum_source_messages, sum_outgoing_messages;

    public double getCpu_now() {
        return cpu_now;
    }

    public void setCpu_now(double cpu_now) {
        this.cpu_now = cpu_now;
    }

    public double getCpu_10() {
        return cpu_10;
    }

    public void setCpu_10(double cpu_10) {
        this.cpu_10 = cpu_10;
    }

    public double getCpu_20() {
        return cpu_20;
    }

    public void setCpu_20(double cpu_20) {
        this.cpu_20 = cpu_20;
    }

    public double getRam_now() {
        return ram_now;
    }

    public void setRam_now(double ram_now) {
        this.ram_now = ram_now;
    }

    public double getRam_10() {
        return ram_10;
    }

    public void setRam_10(double ram_10) {
        this.ram_10 = ram_10;
    }

    public double getRam_20() {
        return ram_20;
    }

    public void setRam_20(double ram_20) {
        this.ram_20 = ram_20;
    }

    public double getNetwork_in() {
        return network_in;
    }

    public void setNetwork_in(double network_in) {
        this.network_in = network_in;
    }

    public double getNetwork_out() {
        return network_out;
    }

    public void setNetwork_out(double network_out) {
        this.network_out = network_out;
    }

    public double getRound_trip() {
        return round_trip;
    }

    public void setRound_trip(double round_trip) {
        this.round_trip = round_trip;
    }

    public double getRate_source_consumption_now() {
        return rate_source_consumption_now;
    }

    public void setRate_source_consumption_now(double rate_source_consumption_now) {
        this.rate_source_consumption_now = rate_source_consumption_now;
    }

    public double getRate_source_consumption_10() {
        return rate_source_consumption_10;
    }

    public void setRate_source_consumption_10(double rate_source_consumption_10) {
        this.rate_source_consumption_10 = rate_source_consumption_10;
    }

    public double getRate_source_consumption_20() {
        return rate_source_consumption_20;
    }

    public void setRate_source_consumption_20(double rate_source_consumption_20) {
        this.rate_source_consumption_20 = rate_source_consumption_20;
    }

    public double getRate_sent_now() {
        return rate_sent_now;
    }

    public void setRate_sent_now(double rate_sent_now) {
        this.rate_sent_now = rate_sent_now;
    }

    public double getRate_sent_10() {
        return rate_sent_10;
    }

    public void setRate_sent_10(double rate_sent_10) {
        this.rate_sent_10 = rate_sent_10;
    }

    public double getRate_sent_20() {
        return rate_sent_20;
    }

    public void setRate_sent_20(double rate_sent_20) {
        this.rate_sent_20 = rate_sent_20;
    }

    public boolean isKilled_process() {
        return killed_process;
    }

    public void setKilled_process(boolean killed_process) {
        this.killed_process = killed_process;
    }

    public int getSum_source_messages() {
        return sum_source_messages;
    }

    public void setSum_source_messages(int sum_source_messages) {
        this.sum_source_messages = sum_source_messages;
    }

    public int getSum_outgoing_messages() {
        return sum_outgoing_messages;
    }

    public void setSum_outgoing_messages(int sum_outgoing_messages) {
        this.sum_outgoing_messages = sum_outgoing_messages;
    }

    @Override
    public String toString() {
        return "SingleOperatorStatistics{" +
                "cpu_now=" + cpu_now +
                ", ram_now=" + ram_now +
                ", rate_source_consumption_now=" + rate_source_consumption_now +
                ", rate_sent_now=" + rate_sent_now +
                ", killed_process=" + killed_process +
                '}';
    }
}
