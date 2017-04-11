package net.knasmueller.pathfinder.controller;


import net.knasmueller.pathfinder.service.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ForwardController {
    private static final Logger LOG = LoggerFactory.getLogger(StatusController.class);

    @Autowired
    private CircuitBreaker circuitBreaker;

    @RequestMapping(value = "/users", method= RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Object checkStatus() {
        LOG.debug("Call to /users");

        if(!circuitBreaker.isClosed()) {
            Map<String, Object> resultMap = new HashMap<>();
            LOG.warn("CB open, fail fast");
            resultMap.put("status", "Circuit open");
            return resultMap;
        } else {
            RestTemplate restTemplate = new RestTemplate();
            String resultMap = restTemplate.getForObject("https://jsonplaceholder.typicode.com/users", String.class);
            LOG.debug("CB closed, serving content");
            return resultMap;
        }
    }
}
