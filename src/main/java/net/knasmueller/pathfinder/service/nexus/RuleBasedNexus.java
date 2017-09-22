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
        if(s.getItemsWaiting() > 20 && s.getActualCpuCores() < 0.1) {
            // process does no work despite full queue
            return OperatorClassification.FAILED;
        }

        if(s.getActualMemory() < 150 && s.getActualCpuCores() < 0.1) {
            // too small resource usage for productive work
            return OperatorClassification.FAILED;
        }

        if(s.getItemsWaiting() > 100 && s.getDeliveryRate() < 0.5) {
            // no output despite full queue
            return OperatorClassification.FAILED;
        }

        if(s.getDeliveryRate() < 0.5 && s.getActualCpuCores() > 1.6) {
            // process most likely stuck in a loop
            return OperatorClassification.FAILED;
        }

        return OperatorClassification.WORKING;
    }

}
