package net.knasmueller.pathfinder.unit_tests;

import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;
import net.knasmueller.pathfinder.exceptions.InvalidCircuitBreakerTransition;
import net.knasmueller.pathfinder.service.CircuitBreaker;
import net.knasmueller.pathfinder.service.nexus.INexus;
import net.knasmueller.pathfinder.service.nexus.RuleBasedNexus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class RuleBasedNexusTests {
    RuleBasedNexus ruleBasedNexus;
    @Before
    public void init() {
        ruleBasedNexus = new RuleBasedNexus();
    }

    @Test
    public void test_lowSourceConsumption_isFailed() {
        SingleOperatorStatistics statistics = SingleOperatorStatistics.fromDefault();
        statistics.setIncomingRate(0.0);
        statistics.setItemsWaiting(150);
        statistics.setDeliveryRate(0.0);
        assert(ruleBasedNexus.predict(statistics).equals(INexus.OperatorClassification.FAILED));
    }

    @Test
    public void test_lowCpuLowRam_isFailed() {
        SingleOperatorStatistics statistics = SingleOperatorStatistics.fromDefault();
        statistics.setActualMemory(0);
        statistics.setActualCpuCores(0.0);
        assert(ruleBasedNexus.predict(statistics).equals(INexus.OperatorClassification.FAILED));
    }

    @Test
    public void test_defaultOperator_isWorking() {
        SingleOperatorStatistics statistics = SingleOperatorStatistics.fromDefault();
        assert(ruleBasedNexus.predict(statistics).equals(INexus.OperatorClassification.WORKING));
    }




}
