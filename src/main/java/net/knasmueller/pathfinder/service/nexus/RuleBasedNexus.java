package net.knasmueller.pathfinder.service.nexus;


import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;
import org.springframework.stereotype.Service;

/**
 * An INexus implementation based on fixed rules
 * Uses only a selected subset of the statistical parameters for the classification task
 */

@Service
public class RuleBasedNexus implements INexus {
    @Override
    public OperatorClassification predict(SingleOperatorStatistics s) {
        if(s.isKilled_process()) {
            return OperatorClassification.FAILED;
        }
        double maxSourceConsumption = Double.max(Double.max(s.getRate_source_consumption_now(),
                s.getRate_source_consumption_10()), s.getRate_source_consumption_20());
        if(maxSourceConsumption < 0.1) {
            return OperatorClassification.FAILED;
        }

        double maxRam = max3(s.getRam_now(), s.getRam_10(), s.getRam_20());
        double maxCpu = max3(s.getCpu_now(), s.getCpu_10(), s.getCpu_20());

        if(maxRam < 0.25 && maxCpu < 0.1) {
            return OperatorClassification.FAILED;
        }

        return OperatorClassification.WORKING;
    }

    private double max3(double a, double b, double c) {
        return Double.max(Double.max(a, b), c);
    }
}
