package net.knasmueller.pathfinder.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class StatusController {

    private static final Logger LOG = LoggerFactory.getLogger(StatusController.class);

    @RequestMapping("/checkStatus")
    public String checkStatus() {
        LOG.debug("Call to /checkStatus");
        return "online";
    }
}