package net.knasmueller.pathfinder.integration_tests;

import net.knasmueller.pathfinder.exceptions.UnknownOperatorException;
import net.knasmueller.pathfinder.service.SplitDecisionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SplitDecisionServiceTests {
    @SpyBean
    private SplitDecisionService splitDecisionService;



    @Value("classpath:topologies/split_join.conf")
    private Resource splitJoinTopology;

    private static final Logger LOG = LoggerFactory.getLogger(SplitDecisionServiceTests.class);

    @Before
    public void setup() throws IOException {
        splitDecisionService.clear();

    }


    @Test
    public void test_allCircuitBreakersClosed_returnsCorrectStatus() {
        List<String> operators = Arrays.asList("step1", "step2", "step3");
        splitDecisionService.addSplitOperators(operators);

        Assert.assertTrue(splitDecisionService.isClosed("step1"));
        Assert.assertTrue(splitDecisionService.isClosed("step2"));
        Assert.assertTrue(splitDecisionService.isClosed("step3"));
    }

    @Test(expected = UnknownOperatorException.class)
    public void test_emptySplitOperatorSet_exceptionIsThrown() {
        splitDecisionService.isClosed("unknown");
    }

    @Test
    public void test_circuitBreakerStatusIsChanged_changeIsStored() {
        splitDecisionService.addSplitOperators(Arrays.asList("step1", "step2", "step3"));
        splitDecisionService.open("step1");

        Assert.assertTrue(splitDecisionService.isOpen("step1"));
        Assert.assertFalse(splitDecisionService.isOpen("step2"));
        Assert.assertFalse(splitDecisionService.isOpen("step3"));
    }

    @Test
    public void test_changeMultiple_changeIsStored() {
        splitDecisionService.addSplitOperators(Arrays.asList("step1", "step2", "step3"));
        splitDecisionService.open("step1");
        splitDecisionService.open("step2");
        splitDecisionService.open("step3");
        splitDecisionService.close("step3");

        Assert.assertTrue(splitDecisionService.isOpen("step1"));
        Assert.assertTrue(splitDecisionService.isOpen("step2"));
        Assert.assertFalse(splitDecisionService.isOpen("step3"));
    }




}
