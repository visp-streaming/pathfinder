package net.knasmueller.pathfinder.integration_tests;

import ac.at.tuwien.infosys.visp.common.operators.Join;
import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.common.operators.Split;
import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.exceptions.UnknownOperatorException;
import net.knasmueller.pathfinder.service.ProcessingOperatorManagement;
import net.knasmueller.pathfinder.service.Scheduler;
import net.knasmueller.pathfinder.service.SplitManagement;
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
import java.util.*;

import static net.knasmueller.pathfinder.TestUtil.resourceToString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SplitManagementTests {
    @SpyBean
    private SplitManagement splitManagement;



    @Value("classpath:topologies/split_join.conf")
    private Resource splitJoinTopology;

    private static final Logger LOG = LoggerFactory.getLogger(SplitManagementTests.class);

    @Before
    public void setup() throws IOException {
        splitManagement.clear();

    }


    @Test
    public void test_allCircuitBreakersClosed_returnsCorrectStatus() {
        List<String> operators = Arrays.asList("step1", "step2", "step3");
        splitManagement.addSplitOperators(operators);

        Assert.assertTrue(splitManagement.isClosed("step1"));
        Assert.assertTrue(splitManagement.isClosed("step2"));
        Assert.assertTrue(splitManagement.isClosed("step3"));
    }

    @Test(expected = UnknownOperatorException.class)
    public void test_emptySplitOperatorSet_exceptionIsThrown() {
        splitManagement.isClosed("unknown");
    }

    @Test
    public void test_circuitBreakerStatusIsChanged_changeIsStored() {
        splitManagement.addSplitOperators(Arrays.asList("step1", "step2", "step3"));
        splitManagement.open("step1");

        Assert.assertTrue(splitManagement.isOpen("step1"));
        Assert.assertFalse(splitManagement.isOpen("step2"));
        Assert.assertFalse(splitManagement.isOpen("step3"));
    }




}
