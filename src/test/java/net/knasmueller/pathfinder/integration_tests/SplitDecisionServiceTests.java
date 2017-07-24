package net.knasmueller.pathfinder.integration_tests;

import net.knasmueller.pathfinder.domain.ICircuitBreakerStatusProvider;
import net.knasmueller.pathfinder.domain.IDataFlowProvider;
import net.knasmueller.pathfinder.domain.IMessageFlowSwitcher;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.exceptions.NoAlternativePathAvailableException;
import net.knasmueller.pathfinder.exceptions.UnknownOperatorException;
import net.knasmueller.pathfinder.service.CircuitBreaker;
import net.knasmueller.pathfinder.service.ProcessingOperatorHealth;
import net.knasmueller.pathfinder.service.SplitDecisionService;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static net.knasmueller.pathfinder.TestUtil.resourceToString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SplitDecisionServiceTests {
    @SpyBean
    private SplitDecisionService splitDecisionService;

    @SpyBean
    private ProcessingOperatorHealth poh;

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

    @Test
    public void test_circuitBreakerMessageFlow() {
        try {
            doReturn("first").doReturn("second").when(this.poh).getBestAvailablePath(any());
            doReturn("parent").when(this.splitDecisionService).getParentSplitOperator(any());

            IDataFlowProvider dataFlowProvider = () -> {
                Map<String, String> flows = new HashMap<>();
                flows.put("step1", "HALF_OPEN");
                flows.put("step2", "CLOSED");
                flows.put("step3", "CLOSED");
                flows.put("step4", "CLOSED");

                return flows;
            };

            ICircuitBreakerStatusProvider cbStatusProvider = () -> {
                Map<String, CircuitBreaker> result = new HashMap<>();

                for(String s : Arrays.asList("step1", "step2", "step3", "step4")) {
                    CircuitBreaker c = new CircuitBreaker();
                    if(s.equals("step2")) {
                        c.open();
                    } else {
                        c.close();
                    }
                    result.put(s, c);
                }

                return result;
            };


            IMessageFlowSwitcher messageFlowSwitcher = mock(IMessageFlowSwitcher.class);

            splitDecisionService.updateMessageFlowAfterCircuitBreakerUpdate(dataFlowProvider, cbStatusProvider, messageFlowSwitcher);

            verify(messageFlowSwitcher, times(2)).resumeMessageFlow(any(), any());

            verify(messageFlowSwitcher, times(1)).stopMessageFlow(any(), eq("step2"));
            verify(messageFlowSwitcher, times(1)).resumeMessageFlow(any(), eq("first"));
        } catch (NoAlternativePathAvailableException e) {
            e.printStackTrace();
        } catch (EmptyTopologyException e) {
            e.printStackTrace();
        }


    }



}
