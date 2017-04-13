package net.knasmueller.pathfinder.integration_tests;

import net.knasmueller.pathfinder.controller.CommunicationController;
import net.knasmueller.pathfinder.service.Communicator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommunicatorTests {
    @SpyBean
    private Communicator communicator;

    private static final Logger LOG = LoggerFactory.getLogger(CommunicatorTests.class);

    @Before
    public void setup() {

    }

    @Test
    public void test_threeSiblingsAreKnown_operatorPropagationReachesAllOfThem() {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                LOG.info("sendOperatorStatusUpdateToSibling was called! third param: " + ((String) arguments[2]));
                return null;
            }
        }).when(communicator).sendOperatorStatusUpdateToSibling(anyString(), anyString(), anyString());
        communicator.addSibling("127.0.0.1:1234");
        communicator.addSibling("127.0.0.1:1235");
        communicator.addSibling("127.0.0.1:1236");
        communicator.propagateOperatorStatus("operator1", "working");

        verify(communicator, times(3)).sendOperatorStatusUpdateToSibling(any(), any(), any());
    }



}
