package net.knasmueller.pathfinder.controller;


import net.knasmueller.pathfinder.service.CircuitBreaker;
import net.knasmueller.pathfinder.service.Communicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/communication")
public class CommunicationController {
    private static final Logger LOG = LoggerFactory.getLogger(CommunicationController.class);

    @Autowired
    private Communicator communicator;

    @RequestMapping("/addSibling")
    public String addSibling(@RequestParam(value = "endpoint") String endpoint) {
        communicator.addSibling(endpoint);
        return "ok";
    }

    @RequestMapping("/removeSibling")
    public String removeSibling(@RequestParam(value = "endpoint") String endpoint) {
        communicator.removeSibling(endpoint);
        return "ok";
    }

    @RequestMapping("/getSiblings")
    @ResponseBody
    public List<String> getSiblings() {
        return communicator.getSiblings();
    }
}
