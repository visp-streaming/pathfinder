package net.knasmueller.pathfinder.integration_tests;

import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.exceptions.VispRuntimeUnavailableException;
import net.knasmueller.pathfinder.service.Scheduler;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VispIntegrationTests {
    @MockBean
    private VispCommunicator vispCommunicator;


    @Autowired
    private Scheduler scheduler;

    @Before
    public void setup() {

    }

    @Test
    public void test_onlyOneVispRuntime_topologyIsFetched() throws VispRuntimeUnavailableException {
        given(this.vispCommunicator.
                getTopologyFromVisp(new VispRuntimeIdentifier("127.0.0.1:1234"))
        ).willReturn(
                getExampleTopology());

        List<VispRuntimeIdentifier> runtimes = new ArrayList<>();
        runtimes.add(new VispRuntimeIdentifier("127.0.0.1", 1234));
        given(this.vispCommunicator.getVispRuntimeIdentifiers()).willReturn(runtimes);
        given(this.vispCommunicator.getCachedTopologyString()).willReturn("");

        scheduler.maybePullTopologyUpdate();
        verify(vispCommunicator).setCachedTopologyString(any());
        verify(vispCommunicator).getTopologyFromVisp(new VispRuntimeIdentifier("127.0.0.1", 1234));
    }

    @Test
    public void test_threeVispRuntimes_topologyIsFetchedOnlyOnce() throws VispRuntimeUnavailableException {
        given(this.vispCommunicator.
                getTopologyFromVisp(any())
        ).willReturn(
                getExampleTopology());

        List<VispRuntimeIdentifier> runtimes = new ArrayList<>();
        runtimes.add(new VispRuntimeIdentifier("127.0.0.1", 1234));
        runtimes.add(new VispRuntimeIdentifier("127.0.0.1", 1235));
        runtimes.add(new VispRuntimeIdentifier("127.0.0.1", 1236));
        given(this.vispCommunicator.getVispRuntimeIdentifiers()).willReturn(runtimes);
        given(this.vispCommunicator.getCachedTopologyString()).willReturn("");

        scheduler.maybePullTopologyUpdate();
        verify(vispCommunicator).setCachedTopologyString(any());
        verify(vispCommunicator).getTopologyFromVisp(new VispRuntimeIdentifier("127.0.0.1", 1234));
        verify(vispCommunicator, times(1)).getTopologyFromVisp(any());
    }

    public static String getExampleTopology() {
        return "$source = Source() {\n" +
                "  concreteLocation = 192.168.0.1/openstackpool,\n" +
                "  type             = source,\n" +
                "  outputFormat     = \"temperature data from sensor XYZ\",\n" +
                "  #meaningless for sources and should be ignored by parser:\n" +
                "  expectedDuration = 15\n" +
                "}";
    }
}
