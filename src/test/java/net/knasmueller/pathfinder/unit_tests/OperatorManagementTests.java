package net.knasmueller.pathfinder.unit_tests;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.topologyParser.TopologyParser;
import net.knasmueller.pathfinder.entities.operator_statistics.SingleOperatorStatistics;
import net.knasmueller.pathfinder.integration_tests.SplitJoinIntegrationTests;
import net.knasmueller.pathfinder.service.OperatorManagement;
import net.knasmueller.pathfinder.service.nexus.INexus;
import net.knasmueller.pathfinder.service.nexus.RuleBasedNexus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
public class OperatorManagementTests {
    OperatorManagement operatorManagement;

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
        operatorManagement = new OperatorManagement();
    }

    @Test
    public void test_topologyWithoutSplitJoin_noPathsExtracted() throws IOException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(simpleTopology.getFile().getAbsolutePath()).topology;
        Assert.assertTrue(topology.keySet().contains("source"));
        Assert.assertTrue(topology.keySet().contains("step1"));
        Assert.assertTrue(topology.keySet().contains("step2"));
        Assert.assertTrue(topology.keySet().contains("log"));

        Map<String, List<String>> alternativePaths = operatorManagement.getAlternativePaths(topology);

        Assert.assertTrue(alternativePaths.size() == 0);
    }

    @Test
    public void test_topologyWithSplitJoin_pathIsExtracted() throws IOException {
        Map<String, Operator> topology =
                topologyParser.parseTopologyFromFileSystem(splitJoinTopology.getFile().getAbsolutePath()).topology;
        Assert.assertTrue(topology.keySet().contains("source"));
        Assert.assertTrue(topology.keySet().contains("step1"));
        Assert.assertTrue(topology.keySet().contains("split"));
        Assert.assertTrue(topology.keySet().contains("join"));
        Assert.assertTrue(topology.keySet().contains("step2a"));
        Assert.assertTrue(topology.keySet().contains("step2b"));
        Assert.assertTrue(topology.keySet().contains("log"));

        Map<String, List<String>> alternativePaths = operatorManagement.getAlternativePaths(topology);

        Assert.assertTrue(alternativePaths.containsKey("split"));
        Assert.assertTrue(alternativePaths.get("split").contains("step2a"));
        Assert.assertTrue(alternativePaths.get("split").contains("step2b"));
    }

    @Test
    public void test_topologyWith2SplitJoins_2pathsAreExtracted() throws IOException {
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

        Map<String, List<String>> alternativePaths = operatorManagement.getAlternativePaths(topology);

        Assert.assertTrue(alternativePaths.containsKey("split"));
        Assert.assertTrue(alternativePaths.get("split").contains("step2a"));
        Assert.assertTrue(alternativePaths.get("split").contains("step2b"));

        Assert.assertTrue(alternativePaths.containsKey("split2"));
        Assert.assertTrue(alternativePaths.get("split2").get(0).equals("step4b"));
        Assert.assertTrue(alternativePaths.get("split2").get(1).equals("step4a"));
    }
}
