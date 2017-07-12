package net.knasmueller.pathfinder.controller.webfrontend;

import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.repository.SingleOperatorStatisticsRepository;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/webfrontend")
public class PathFinderStatisticsController {


    @Value("${server.port}")
    String port;

    @Autowired
    private VispCommunicator vispCommunicator;

    @Autowired
    private SingleOperatorStatisticsRepository sosr;

    private static final Logger LOG = LoggerFactory.getLogger(PathFinderStatisticsController.class);

    @RequestMapping("/getStatistics")
    public HashMap<Object, Object> getStatistics() {
        LOG.debug("Call to /getStatistics");
        HashMap<Object, Object> result = new HashMap<>();

        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long uptime_ms = rb.getUptime();

        String uptime_string = String.format("%dh, %dm",
                TimeUnit.MILLISECONDS.toHours(uptime_ms),
                TimeUnit.MILLISECONDS.toMinutes(uptime_ms) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptime_ms))
        );

        List<VispRuntimeIdentifier> vispRuntimes = vispCommunicator.getVispRuntimeIdentifiers();

        result.put("ip", "127.0.0.1");
        result.put("port", port);
        result.put("instances", vispRuntimes == null ? 0 : vispRuntimes.size());
        result.put("dbentries", sosr.count());
        result.put("uptime", uptime_string);
        result.put("version", "0.2.1");

        return result;
    }

    @RequestMapping("/getRuntimeData")
    public HashMap<Object, Object> getRuntimedata() {
        LOG.debug("Call to /getRuntimedata");
        HashMap<Object, Object> result = new HashMap<>();

        List<HashMap<Object, Object>> instancesList = new ArrayList<>();

        List<VispRuntimeIdentifier> vispRuntimes = vispCommunicator.getVispRuntimeIdentifiers();

        int counter = 1;
        for(VispRuntimeIdentifier rti : vispRuntimes) {
            HashMap<Object, Object> instance = new HashMap<>();
            instance.put("id", counter++);
            instance.put("ip", rti.getIp());
            instance.put("port", rti.getPort());
            instancesList.add(instance);
        }

        result.put("instances", instancesList);

        return result;
    }
}