package net.knasmueller.pathfinder.integration_tests;

import net.knasmueller.pathfinder.entities.TopologyStability;
import net.knasmueller.pathfinder.repository.TopologyStabilityRepository;
import net.knasmueller.pathfinder.service.ProcessingOperatorHealth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TopologyStabilityTests {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyStabilityTests.class);

    @Autowired
    TopologyStabilityRepository tsr;

    @Autowired
    ProcessingOperatorHealth poh;

    @Before
    public void init() {

    }

    @Test
    public void test_topologyStabilityPersistence() throws IOException {
        TopologyStability ts = new TopologyStability("1517af127", 0.9);
        tsr.save(ts);


        Assert.assertTrue(poh.getStabilityTop10("1517af127").get(0).getStability() - 0.9 < 0.0001);
    }



}
