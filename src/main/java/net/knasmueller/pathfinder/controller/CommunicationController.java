package net.knasmueller.pathfinder.controller;


import net.knasmueller.pathfinder.service.PathFinderCommunicator;
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
    private PathFinderCommunicator pathFinderCommunicator;

    @RequestMapping("/addSibling")
    public String addSibling(@RequestParam(value = "endpoint") String endpoint) {
        pathFinderCommunicator.addSibling(endpoint);
        return "ok";
    }

    @RequestMapping("/removeSibling")
    public String removeSibling(@RequestParam(value = "endpoint") String endpoint) {
        pathFinderCommunicator.removeSibling(endpoint);
        return "ok";
    }

    @RequestMapping("/getSiblings")
    @ResponseBody
    public List<String> getSiblings() {
        return pathFinderCommunicator.getSiblings();
    }

    @RequestMapping("/propagateOperatorStatus")
    @ResponseBody
    public String propagateOperatorStatus(@RequestParam(value="operatorId") String operatorId, @RequestParam(value="status") String status) {
        // this method automatically notifies all pathfinder instances about a change in some operator's status
        pathFinderCommunicator.propagateOperatorStatus(operatorId, status);
        return "ok";
    }
}
