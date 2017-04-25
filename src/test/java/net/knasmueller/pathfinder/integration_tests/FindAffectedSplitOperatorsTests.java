package net.knasmueller.pathfinder.integration_tests;

import ac.at.tuwien.infosys.visp.topologyParser.TopologyParser;
import net.knasmueller.pathfinder.TestUtil;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.service.Scheduler;
import net.knasmueller.pathfinder.service.VispCommunicator;
import net.knasmueller.pathfinder.service.VispTopology;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FindAffectedSplitOperatorsTests {

    @SpyBean
    private VispCommunicator vispCommunicator;

    TopologyParser topologyParser = new TopologyParser();

    @Autowired
    private Scheduler scheduler;

    @Value("classpath:topologies/split_join.conf")
    private Resource splitJoinTopology;

    @Value("classpath:topologies/split_join3.conf")
    private Resource splitJoinTopology3;

    @Before
    public void setup() {

    }


    @Test(expected = EmptyTopologyException.class)
    public void test_emptyTopology_exceptionIsThrown() throws EmptyTopologyException {
        vispCommunicator.clearStoredTopology();
        vispCommunicator.getAffectedSplitOperators("step1");
    }

    @Test
    public void test_operatorDoesNotAffectAnySplit_emptySetReturned() throws EmptyTopologyException, IOException {
        String topologyString = TestUtil.resourceToString(splitJoinTopology);
        doReturn(new VispTopology(topologyParser.parseTopologyFromString(topologyString).topology)).when(this.vispCommunicator).getVispTopology();
        Assert.assertTrue(vispCommunicator.getAffectedSplitOperators("step1").isEmpty());
    }

    @Test
    public void test_oneOperatorIsAffected_returnParentSplitOperator() throws EmptyTopologyException, IOException {
        String topologyString = TestUtil.resourceToString(splitJoinTopology);
        doReturn(new VispTopology(topologyParser.parseTopologyFromString(topologyString).topology)).when(this.vispCommunicator).getVispTopology();

        Set<Pair<String, String>> affectedOperators3 = vispCommunicator.getAffectedSplitOperators("step2a");
        Assert.assertTrue(affectedOperators3.size() == 1);
        List<Pair<String, String>> affectedOperators3List = affectedOperators3.stream().collect(Collectors.toList());
        Assert.assertTrue(affectedOperators3List.get(0).getFirst().equals("split"));
        Assert.assertTrue(affectedOperators3List.get(0).getSecond().equals("step2a"));
    }

    @Test
    public void test_moreThanOneSplitOperator_theyAreCorrectlyAssigned() throws EmptyTopologyException, IOException {
        String topologyString = TestUtil.resourceToString(splitJoinTopology3);
        doReturn(new VispTopology(topologyParser.parseTopologyFromString(topologyString).topology)).when(this.vispCommunicator).getVispTopology();
        Set<Pair<String, String>> affectedOperators1 = vispCommunicator.getAffectedSplitOperators("step2a");
        Assert.assertTrue(affectedOperators1.size() == 1);
        List<Pair<String, String>> affectedOperators1List = affectedOperators1.stream().collect(Collectors.toList());
        Assert.assertTrue(affectedOperators1List.get(0).getFirst().equals("split"));
        Assert.assertTrue(affectedOperators1List.get(0).getSecond().equals("step2a"));

        Set<Pair<String, String>> affectedOperators2 = vispCommunicator.getAffectedSplitOperators("step5a");
        Assert.assertTrue(affectedOperators2.size() == 1);
        List<Pair<String, String>> affectedOperators2List = affectedOperators2.stream().collect(Collectors.toList());
        Assert.assertTrue(affectedOperators2List.get(0).getFirst().equals("split2"));
        Assert.assertTrue(affectedOperators2List.get(0).getSecond().equals("step5a"));

        Set<Pair<String, String>> affectedOperators3 = vispCommunicator.getAffectedSplitOperators("step6a");
        Assert.assertTrue(affectedOperators3.size() == 1);
        List<Pair<String, String>> affectedOperators3List = affectedOperators3.stream().collect(Collectors.toList());
        Assert.assertTrue(affectedOperators3List.get(0).getFirst().equals("split2"));
        Assert.assertTrue(affectedOperators3List.get(0).getSecond().equals("step5a"));
    }
}
