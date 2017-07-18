package net.knasmueller.pathfinder.controller.webfrontend;

import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.entities.TopologyStability;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.repository.SingleOperatorStatisticsRepository;
import net.knasmueller.pathfinder.repository.TopologyStabilityRepository;
import net.knasmueller.pathfinder.service.GraphvizService;
import net.knasmueller.pathfinder.service.ProcessingOperatorHealth;
import net.knasmueller.pathfinder.service.SplitDecisionService;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/webfrontend")
public class PathFinderStatisticsController {


    @Value("${server.port}")
    String port;

    @Autowired
    private VispCommunicator vispCommunicator;

    @Autowired
    private ProcessingOperatorHealth poh;

    @Autowired
    private SingleOperatorStatisticsRepository sosr;

    @Autowired
    private SplitDecisionService spm;

    @Autowired
    private GraphvizService graphvizService;

    @Autowired
    private TopologyStabilityRepository tsr;

    @Autowired
    private SplitDecisionService sds;

    private static final Logger LOG = LoggerFactory.getLogger(PathFinderStatisticsController.class);

    @RequestMapping("/getStatistics")
    public HashMap<Object, Object> getStatistics() {
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
        HashMap<Object, Object> result = new HashMap<>();

        List<HashMap<Object, Object>> instancesList = new ArrayList<>();

        List<VispRuntimeIdentifier> vispRuntimes = vispCommunicator.getVispRuntimeIdentifiers();

        int counter = 1;
        for (VispRuntimeIdentifier rti : vispRuntimes) {
            HashMap<Object, Object> instance = new HashMap<>();
            instance.put("id", counter++);
            instance.put("ip", rti.getIp());
            instance.put("port", rti.getPort());
            instancesList.add(instance);
        }

        result.put("instances", instancesList);

        return result;
    }

    @RequestMapping("/getTopology")
    public HashMap<Object, Object> getTopology() throws UnsupportedEncodingException {
        HashMap<Object, Object> result = new HashMap<>();

        byte[] encodedBytes = Base64.getEncoder().encode(vispCommunicator.getCachedTopologyString().getBytes());


        result.put("topology", new String(encodedBytes, "UTF8"));

        result.put("dotContent", graphvizService.getDotFormatForFrontend(vispCommunicator.getVispTopology()));

        return result;
    }

    @RequestMapping("/getOperators")
    public HashMap<Object, Object> getOperators() {
        HashMap<Object, Object> result = new HashMap<>();

        List<HashMap<Object, Object>> operatorsList = new ArrayList<>();

        HashMap<String, PathfinderOperator> operators = poh.getOperators();

        for (String operatorId : operators.keySet()) {
            if (operators.get(operatorId).getSubclass().equals(PathfinderOperator.Subclass.SPLIT) || operators.get(operatorId).getSubclass().equals(PathfinderOperator.Subclass.JOIN)) {
                continue;
            }
            HashMap<Object, Object> operator = new HashMap<>();
            operator.put("id", operatorId);
            operator.put("concreteLocation", operators.get(operatorId).getConcreteLocation().toString());
            operator.put("operatorStatus", operators.get(operatorId).getStatus().toString().toLowerCase());
            operator.put("subclass", operators.get(operatorId).getSubclass().toString().toUpperCase());
            operatorsList.add(operator);
        }

        result.put("operators", operatorsList);

        List<HashMap<Object, Object>> splitOperatorsList = new ArrayList<>();

        Map<String, List<String>> splitOperatorIds = sds.getAlternativePaths();


        for (String operatorId : splitOperatorIds.keySet()) {
            HashMap<Object, Object> splitOperator = new HashMap<>();
            splitOperator.put("id", operatorId);
            splitOperator.put("activePath", splitOperatorIds.get(operatorId).get(0));
            splitOperator.put("totalPaths", splitOperatorIds.get(operatorId).size());
            splitOperator.put("failedPaths", -1);
            splitOperator.put("availablePaths", -1);

            // TODO: replace by querying real values

            splitOperatorsList.add(splitOperator);
        }


        result.put("splitOperators", splitOperatorsList);
        return result;
    }

    @RequestMapping("/getTopologyStabilizationStatistics")
    public List<HashMap<Object, Object>> getTopologyStabilizationStatistics() {
        List<HashMap<Object, Object>> result = new ArrayList<>();

        String topologyHash = null;
        try {
            topologyHash = vispCommunicator.getVispTopology().getHash();
        } catch (EmptyTopologyException e) {
            return result;
        }

        List<TopologyStability> stabilityList = sds.getStabilityTop10(topologyHash);

        for (TopologyStability stabilityValue : stabilityList) {
            HashMap<Object, Object> hashmap = new HashMap<>();

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(stabilityValue.getTimestamp());

            hashmap.put("y", timestamp);
            hashmap.put("a", stabilityValue.getStability());


            result.add(hashmap);
        }


        return result;
    }

    @RequestMapping("/getSplitOperatorDetails")
    public HashMap<Object, Object> getSplitOperatorDetails(@RequestParam(value = "operatorId") String operatorId) throws UnsupportedEncodingException {
        HashMap<Object, Object> result = new HashMap<>();

        List<String> paths = sds.getAlternativePaths().get(operatorId);

        if(paths == null || paths.isEmpty()) {
            return result;
        }

        HashMap<String, String> circuitBreakerStatus = new HashMap<>();

        for(String s : paths) {
            circuitBreakerStatus.put(s, sds.getCircuitBreakerOfPath(s).toString());
        }


        result.put("paths", paths);
        result.put("circuitBreakerStatus", circuitBreakerStatus);

        return result;
    }


}
