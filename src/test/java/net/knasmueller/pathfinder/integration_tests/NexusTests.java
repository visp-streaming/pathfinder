package net.knasmueller.pathfinder.integration_tests;

import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;
import net.knasmueller.pathfinder.repository.SingleOperatorStatisticsRepository;
import net.knasmueller.pathfinder.service.Scheduler;
import net.knasmueller.pathfinder.service.VispCommunicator;
import net.knasmueller.pathfinder.service.nexus.INexus;
import net.knasmueller.pathfinder.service.nexus.RuleBasedNexus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NexusTests {
    @SpyBean
    private VispCommunicator vispCommunicator;

    private static final Logger LOG = LoggerFactory.getLogger(NexusTests.class);

    @Autowired
    Scheduler scheduler;

    @Autowired
    SingleOperatorStatisticsRepository sosr;


    @Before
    public void setup() {

    }

    @Test
    public void test_givenSomeStatistics_makeCorrectPredictions() {
        OperatorStatisticsResponse r = new OperatorStatisticsResponse();
        r.put("source", SingleOperatorStatistics.fromDefault());
        r.put("step1", SingleOperatorStatistics.fromDefault());
        r.put("step2", SingleOperatorStatistics.fromDefault());
        r.put("step3", SingleOperatorStatistics.fromDefault());


        List<VispRuntimeIdentifier> runtimes = new ArrayList<>();
        runtimes.add(new VispRuntimeIdentifier("127.0.0.1:1234"));

        doReturn(r).when(this.vispCommunicator).getStatisticsFromVisp(any());
        doReturn(runtimes).when(this.vispCommunicator).getVispRuntimeIdentifiers();
        doReturn(VispIntegrationTests.getExampleTopology()).when(vispCommunicator).getTopologyFromVisp(any());
        scheduler.getStatisticsFromAllRuntimes();

        RuleBasedNexus ruleBasedNexus = new RuleBasedNexus();

        Iterable<SingleOperatorStatistics> persisted = sosr.findAll();
        for(SingleOperatorStatistics s : persisted) {
            assert(ruleBasedNexus.predict(s).equals(INexus.OperatorClassification.WORKING));
        }

    }


}
