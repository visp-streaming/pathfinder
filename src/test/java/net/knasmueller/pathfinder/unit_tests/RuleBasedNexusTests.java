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
    public void test_killedProcess_isFailed() {
        SingleOperatorStatistics statistics = SingleOperatorStatistics.fromDefault();
        statistics.setKilled_process(true);
        assert(ruleBasedNexus.predict(statistics).equals(INexus.OperatorClassification.FAILED));
    }

    @Test
    public void test_lowSourceConsumption_isFailed() {
        SingleOperatorStatistics statistics = SingleOperatorStatistics.fromDefault();
        statistics.setRate_source_consumption_now(0.0);
        statistics.setRate_source_consumption_10(0.0);
        statistics.setRate_source_consumption_20(0.0);
        assert(ruleBasedNexus.predict(statistics).equals(INexus.OperatorClassification.FAILED));
    }

    @Test
    public void test_lowCpuLowRam_isFailed() {
        SingleOperatorStatistics statistics = SingleOperatorStatistics.fromDefault();
        statistics.setCpu_now(0.0);
        statistics.setCpu_10(0.005);
        statistics.setCpu_20(0.01);
        statistics.setRam_now(0.2);
        statistics.setRam_10(0.205);
        statistics.setRam_20(0.109);
        assert(ruleBasedNexus.predict(statistics).equals(INexus.OperatorClassification.FAILED));
    }

    @Test
    public void test_defaultOperator_isWorking() {
        SingleOperatorStatistics statistics = SingleOperatorStatistics.fromDefault();
        assert(ruleBasedNexus.predict(statistics).equals(INexus.OperatorClassification.WORKING));
    }




}
