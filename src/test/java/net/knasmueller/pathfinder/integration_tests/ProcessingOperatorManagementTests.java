package net.knasmueller.pathfinder.integration_tests;

import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import net.knasmueller.pathfinder.service.ProcessingOperatorManagement;
import net.knasmueller.pathfinder.service.Scheduler;
import net.knasmueller.pathfinder.service.VispCommunicator;
import net.knasmueller.pathfinder.service.nexus.INexus;
import net.knasmueller.pathfinder.service.nexus.RuleBasedNexus;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.knasmueller.pathfinder.TestUtil.resourceToString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProcessingOperatorManagementTests {
    @SpyBean
    private VispCommunicator vispCommunicator;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ProcessingOperatorManagement processingOperatorManagement;

    RuleBasedNexus rbn;

    @Value("classpath:topologies/split_join.conf")
    private Resource splitJoinTopology;

    @Value("classpath:topologies/split_join3.conf")
    private Resource splitJoinTopology3;

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingOperatorManagementTests.class);

    OperatorStatisticsResponse unavailableStatistics;

    List<String> operatorNames;

    @Before
    public void setup() throws IOException {
        rbn = new RuleBasedNexus();
        doReturn(resourceToString(splitJoinTopology)).when(this.vispCommunicator).getTopologyFromVisp(any());
        List<VispRuntimeIdentifier> runtimes = new ArrayList<>();
        runtimes.add(new VispRuntimeIdentifier("127.0.0.1", 1234));

        doReturn(runtimes).when(this.vispCommunicator).getVispRuntimeIdentifiers();
        doReturn("").when(vispCommunicator).getCachedTopologyString();

        scheduler.checkForTopologyUpdate();

        verify(vispCommunicator).setCachedTopologyString(any());

        operatorNames = new ArrayList<>();
        operatorNames.add("step1");
        operatorNames.add("step2a");
        operatorNames.add("step2b");
        operatorNames.add("step3");
        operatorNames.add("step4");
        unavailableStatistics = OperatorStatisticsResponse.fromSetOfOperatorNamesDefault(operatorNames);

        doReturn(unavailableStatistics).when(this.vispCommunicator).getStatisticsFromVisp(any());
    }


    @Test
    public void test_operatorGetsUnavailable_isRecognized() throws IOException {
        HashMap<String, PathfinderOperator> topology = processingOperatorManagement.getOperators();

        OperatorStatisticsResponse statistics = vispCommunicator.getStatisticsFromVisp(null);
        Assert.assertTrue(statistics.size() == 5);

        // each operator should be working by default
        for(String operatorName : operatorNames) {
            Assert.assertTrue(rbn.predict(statistics.get(operatorName)).equals(INexus.OperatorClassification.WORKING));
        }

        // kill step2a
        unavailableStatistics.get("step2a").setKilled_process(true);

        doReturn(unavailableStatistics).when(this.vispCommunicator).getStatisticsFromVisp(any());

        Assert.assertTrue(rbn.predict(statistics.get("step2a")).equals(INexus.OperatorClassification.FAILED));

    }

    @Test
    public void test_operatorGetsAvailableAgain_isRecognized() throws IOException {
        HashMap<String, PathfinderOperator> topology = processingOperatorManagement.getOperators();

        OperatorStatisticsResponse statistics = vispCommunicator.getStatisticsFromVisp(null);
        Assert.assertTrue(statistics.size() == 5);

        // each operator should be working by default
        for(String operatorName : operatorNames) {
            Assert.assertTrue(rbn.predict(statistics.get(operatorName)).equals(INexus.OperatorClassification.WORKING));
        }

        // kill step2a
        unavailableStatistics.get("step2a").setKilled_process(true);

        doReturn(unavailableStatistics).when(this.vispCommunicator).getStatisticsFromVisp(any());

        Assert.assertTrue(rbn.predict(statistics.get("step2a")).equals(INexus.OperatorClassification.FAILED));

        // restart step2a
        unavailableStatistics.get("step2a").setKilled_process(false);

        doReturn(unavailableStatistics).when(this.vispCommunicator).getStatisticsFromVisp(any());

        Assert.assertTrue(rbn.predict(statistics.get("step2a")).equals(INexus.OperatorClassification.WORKING));

    }


    @Test
    public void test_operatorStatusIsUpdatedSuchThatPathGetsUnavailable_correctAlternativePathIsSentToVisp() throws IOException {
        List<Triple<String, String, String>> parameters = getParametersForAlternativePathTest();
        for(Triple<String, String, String> triple : parameters) {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    Object[] arguments = invocationOnMock.getArguments();
                    List<Pair<String, String>> splitPathPair = (List<Pair<String, String>>) arguments[0];
                    Assert.assertTrue(splitPathPair.size() == 1);
                    Assert.assertEquals(splitPathPair.get(0).getFirst(), triple.getMiddle());
                    Assert.assertEquals(splitPathPair.get(0).getSecond(), triple.getRight());
                    return null;
                }
            }).when(this.vispCommunicator).switchSplitToPath(any());

            Map<String, INexus.OperatorClassification> newAvailabilities = new HashMap<>();
            newAvailabilities.put(triple.getLeft(), INexus.OperatorClassification.FAILED);
            processingOperatorManagement.updateOperatorAvailabilities(newAvailabilities);
        }
    }

    private List<Triple<String, String, String>> getParametersForAlternativePathTest() {
        List<Triple<String, String, String>> result = new ArrayList<>();
        // 1: which operator goes down
        // 2: which split node is affected
        // 3: which path will the affected split node take now
        result.add(Triple.of("step2a", "split", "step2b"));
        return result;
    }


}
