package net.knasmueller.pathfinder.unit_tests;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import ac.at.tuwien.infosys.visp.topologyParser.TopologyParser;
import net.knasmueller.pathfinder.entities.operator_statistics.OperatorStatisticsResponse;
import net.knasmueller.pathfinder.service.OperatorManagement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
public class OperatorStatisticsResponseTests {

    private static final Logger LOG = LoggerFactory.getLogger(OperatorStatisticsResponseTests.class);


    @Before
    public void init() {

    }

    @Test
    public void test_emptyOperatorNameList() throws IOException {
        List<String> emptyList = new ArrayList<>();
        OperatorStatisticsResponse r = OperatorStatisticsResponse.fromSetOfOperatorNamesDefault(emptyList);
        Assert.assertTrue(r.size() == 0);
    }

    @Test
    public void test_operatorListWith1Element_elementIsContainedAndHasValidStatistics() throws IOException {
        List<String> oneList = new ArrayList<>();
        oneList.add("step1");
        OperatorStatisticsResponse r = OperatorStatisticsResponse.fromSetOfOperatorNamesDefault(oneList);
        Assert.assertTrue(r.size() == 1);
        Assert.assertTrue(r.get("step1").getOperatorName().equals("step1"));

        Assert.assertFalse(r.get("step1").isKilled_process());
        Assert.assertTrue(r.get("step1").getRam_now() > 0.3);
    }

}
