package net.knasmueller.pathfinder_deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RestController
@RequestMapping("/deployment")
public class DeploymentController {


    @Autowired
    private OpenstackConnector openstackConnector;

    private static final Logger LOG = LoggerFactory.getLogger(DeploymentController.class);

    private List<String> nodes = new ArrayList<>();

    @RequestMapping("/start")
    public HashMap<Object, Object> start(@RequestParam(defaultValue = "5") String number) {
        int num = Integer.parseInt(number);
        HashMap<Object, Object> result = new HashMap<>();

        for (int i=0; i < num; i++) {
            String name = "bernhard_node" + (i+1);
            nodes.add(name);
            openstackConnector.startInstance(name);
        }

        int maxAttempts = 20;
        boolean success = false;
        for(int i=0; i < maxAttempts; i++) {
            String lastAddress = openstackConnector.getIpByName("bernhard_node" + nodes.size());
            if(lastAddress == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // until openstack is done
                }
            } else {
                success = true;
                LOG.info("Successfully determined IP of last instance after " + i + " second(s)");
                break;
            }
        }

        if(!success) {
            LOG.warn("Could not determine IP of last instance");
        }

        result.put("done", true);

        return result;
    }

    @RequestMapping("/ips")
    public HashMap<Object, Object> getIps() {
        HashMap<Object, Object> result = new HashMap<>();

        for (int i=0; i < nodes.size(); i++) {
            result.put(nodes.get(i), openstackConnector.getIpByName(nodes.get(i)));
        }

        return result;
    }

    @RequestMapping("/stop")
    public HashMap<Object, Object> stop() {
        HashMap<Object, Object> result = new HashMap<>();

        for (int i=0; i < nodes.size(); i++) {
            openstackConnector.destroyInstanceByName(nodes.get(i));
        }

        nodes.clear();

        result.put("done", true);

        return result;
    }


}
