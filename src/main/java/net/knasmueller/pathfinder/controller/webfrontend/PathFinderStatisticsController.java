package net.knasmueller.pathfinder.controller.webfrontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;



@RestController
@RequestMapping("/webfrontend")
public class PathFinderStatisticsController {


    @Value("$server.port")
    String port;

    private static final Logger LOG = LoggerFactory.getLogger(PathFinderStatisticsController.class);

    @RequestMapping("/getStatistics")
    public HashMap<Object, Object> getStatistics() {
        LOG.debug("Call to /getStatistics");
        HashMap<Object, Object> result = new HashMap<>();

        result.put("ip", "127.0.0.1");
        result.put("port", port);
        result.put("instances", 3);
        result.put("dbentries", 4168);
        result.put("uptime", 42);
        result.put("version", "0.2.1");

        return result;
    }
}