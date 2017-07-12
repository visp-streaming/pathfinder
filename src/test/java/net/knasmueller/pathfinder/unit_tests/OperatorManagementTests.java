package net.knasmueller.pathfinder.unit_tests;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.topologyParser.TopologyParser;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.service.ProcessingOperatorHealth;
import net.knasmueller.pathfinder.service.nexus.INexus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(SpringJUnit4ClassRunner.class)
public class OperatorManagementTests {
    private ProcessingOperatorHealth processingOperatorHealth;


    private static final Logger LOG = LoggerFactory.getLogger(OperatorManagementTests.class);


    @Value("classpath:topologies/simple.conf")
    private Resource simpleTopology;

    @Value("classpath:topologies/split_join.conf")
    private Resource splitJoinTopology;

    @Value("classpath:topologies/split_join2.conf")
    private Resource splitJoinTopology2;

    private TopologyParser topologyParser = new TopologyParser();

    @Before
    public void init() {
        processingOperatorHealth = Mockito.spy(new ProcessingOperatorHealth());
    }

    @Test
    public void test_topologyWithoutSplitJoin_noPathsExtracted() throws IOException, EmptyTopologyException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(simpleTopology.getFile().getAbsolutePath()).topology;
        Assert.assertTrue(topology.keySet().contains("source"));
        Assert.assertTrue(topology.keySet().contains("step1"));
        Assert.assertTrue(topology.keySet().contains("step2"));
        Assert.assertTrue(topology.keySet().contains("log"));

        Map<String, List<String>> alternativePaths = processingOperatorHealth.getAlternativePaths(topology);

        Assert.assertTrue(alternativePaths.size() == 0);
    }

    @Test
    public void test_topologyWithSplitJoin_pathIsExtracted() throws IOException, EmptyTopologyException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(splitJoinTopology.getFile().getAbsolutePath()).topology;
        Assert.assertTrue(topology.keySet().contains("source"));
        Assert.assertTrue(topology.keySet().contains("step1"));
        Assert.assertTrue(topology.keySet().contains("split"));
        Assert.assertTrue(topology.keySet().contains("join"));
        Assert.assertTrue(topology.keySet().contains("step2a"));
        Assert.assertTrue(topology.keySet().contains("step2b"));
        Assert.assertTrue(topology.keySet().contains("log"));

        Map<String, List<String>> alternativePaths = processingOperatorHealth.getAlternativePaths(topology);

        Assert.assertTrue(alternativePaths.containsKey("split"));
        Assert.assertTrue(alternativePaths.get("split").contains("step2a"));
        Assert.assertTrue(alternativePaths.get("split").contains("step2b"));
    }

    @Test
    public void test_topologyWith2SplitJoins_2pathsAreExtracted() throws IOException, EmptyTopologyException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(splitJoinTopology2.getFile().getAbsolutePath()).topology;
        Assert.assertTrue(topology.keySet().contains("source"));
        Assert.assertTrue(topology.keySet().contains("step1"));
        Assert.assertTrue(topology.keySet().contains("split"));
        Assert.assertTrue(topology.keySet().contains("join"));
        Assert.assertTrue(topology.keySet().contains("step2a"));
        Assert.assertTrue(topology.keySet().contains("step2b"));
        Assert.assertTrue(topology.keySet().contains("split2"));
        Assert.assertTrue(topology.keySet().contains("step4a"));
        Assert.assertTrue(topology.keySet().contains("step4b"));
        Assert.assertTrue(topology.keySet().contains("join2"));
        Assert.assertTrue(topology.keySet().contains("log"));

        Map<String, List<String>> alternativePaths = processingOperatorHealth.getAlternativePaths(topology);

        Assert.assertTrue(alternativePaths.containsKey("split"));
        Assert.assertTrue(alternativePaths.get("split").contains("step2a"));
        Assert.assertTrue(alternativePaths.get("split").contains("step2b"));

        Assert.assertTrue(alternativePaths.containsKey("split2"));
        Assert.assertTrue(alternativePaths.get("split2").get(0).equals("step4b"));
        Assert.assertTrue(alternativePaths.get("split2").get(1).equals("step4a"));
    }

    @Test
    public void test_operatorStatusUpdateOneOperator_updateIsPerformed() throws IOException {

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        }).when(processingOperatorHealth).contactVispWithNewRecommendations(any());

        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(splitJoinTopology.getFile().getAbsolutePath()).topology;
        processingOperatorHealth.topologyUpdate(topology);
        Assert.assertTrue(processingOperatorHealth.isOperatorAvailable("step2a"));
        Map<String, INexus.OperatorClassification> update = new HashMap<>();
        update.put("step2a", INexus.OperatorClassification.FAILED);
        processingOperatorHealth.updateOperatorAvailabilities(update);

        Assert.assertFalse(processingOperatorHealth.isOperatorAvailable("step2a"));
    }

    @Test
    public void test_findDownstreamOperators_oneChild() throws IOException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(splitJoinTopology.getFile().getAbsolutePath()).topology;
        Set<String> downstreamOperators = ProcessingOperatorHealth.getDownstreamOperators(topology, "step1");
        Assert.assertTrue(downstreamOperators.contains("split"));
    }

    @Test
    public void test_findDownstreamOperators_source() throws IOException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(splitJoinTopology.getFile().getAbsolutePath()).topology;
        Set<String> downstreamOperators = ProcessingOperatorHealth.getDownstreamOperators(topology, "source");
        Assert.assertTrue(downstreamOperators.contains("step1"));
    }

    @Test
    public void test_findDownstreamOperators_sink() throws IOException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(splitJoinTopology.getFile().getAbsolutePath()).topology;
        Set<String> downstreamOperators = ProcessingOperatorHealth.getDownstreamOperators(topology, "sink");
        Assert.assertTrue(downstreamOperators.isEmpty());
    }

    @Test
    public void test_findDownstreamOperators_twoChildren() throws IOException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(splitJoinTopology.getFile().getAbsolutePath()).topology;
        Set<String> downstreamOperators = ProcessingOperatorHealth.getDownstreamOperators(topology, "split");
        Assert.assertTrue(downstreamOperators.size() == 2);
        Assert.assertTrue(downstreamOperators.contains("step2a"));
        Assert.assertTrue(downstreamOperators.contains("step2b"));
    }
}
