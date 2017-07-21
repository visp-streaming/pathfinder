package net.knasmueller.pathfinder.integration_tests;

import ac.at.tuwien.infosys.visp.common.operators.Join;
import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.service.ProcessingOperatorHealth;
import net.knasmueller.pathfinder.service.Scheduler;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.knasmueller.pathfinder.TestUtil.resourceToString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SplitJoinIntegrationTests {
    @SpyBean
    private VispCommunicator vispCommunicator;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ProcessingOperatorHealth processingOperatorHealth;

    @Value("classpath:topologies/split_join.conf")
    private Resource splitJoinTopology;

    private static final Logger LOG = LoggerFactory.getLogger(SplitJoinIntegrationTests.class);

    @Before
    public void setup() throws IOException {
        doReturn(resourceToString(splitJoinTopology)).when(this.vispCommunicator).getTopologyFromVisp(any());
        List<VispRuntimeIdentifier> runtimes = new ArrayList<>();
        runtimes.add(new VispRuntimeIdentifier("127.0.0.1", 1234));

        doReturn(runtimes).when(this.vispCommunicator).getVispRuntimeIdentifiers();
        doReturn("").when(vispCommunicator).getCachedTopologyString();

        scheduler.maybePullTopologyUpdate();

        verify(vispCommunicator).setCachedTopologyString(any());
    }


    @Test
    public void test_topologyContainsSplitAndJoin_operatorClassesAreCorrectlyRecognized() throws IOException {
        HashMap<String, PathfinderOperator> topology = processingOperatorHealth.getOperators();
        Assert.assertTrue(topology.containsKey("split"));
        Assert.assertTrue(topology.containsKey("join"));

        PathfinderOperator split = topology.get("split");
        PathfinderOperator join = topology.get("join");
        PathfinderOperator step2a = topology.get("step2a");
        PathfinderOperator source = topology.get("source");
        PathfinderOperator log = topology.get("log");

        Assert.assertTrue(PathfinderOperator.Subclass.SPLIT.equals(split.getSubclass()));
        Assert.assertTrue(PathfinderOperator.Subclass.JOIN.equals(join.getSubclass()));
        Assert.assertTrue(PathfinderOperator.Subclass.PROCESSING.equals(step2a.getSubclass()));
        Assert.assertTrue(PathfinderOperator.Subclass.SOURCE.equals(source.getSubclass()));
        Assert.assertTrue(PathfinderOperator.Subclass.SINK.equals(log.getSubclass()));
    }

    @Test
    public void test_pathFinderOperatorsHaveCorrectSubclasses() throws IOException, EmptyTopologyException {
        Map<String, Operator> topology = vispCommunicator.getVispTopology().getTopology();

        Assert.assertTrue(topology.containsKey("split"));
        Assert.assertTrue(topology.containsKey("join"));

        Operator split = topology.get("split");
        Operator join = topology.get("join");

        Assert.assertTrue(split instanceof Split);
        Assert.assertTrue(join instanceof Join);
    }


}
