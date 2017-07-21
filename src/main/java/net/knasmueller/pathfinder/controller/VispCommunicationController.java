package net.knasmueller.pathfinder.controller;


import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/communication")
public class VispCommunicationController {
    /**
     * Each VISP runtime that is added here will be regularily queried by _this_ PathFinder instance
     * A Visp Runtime is uniquely identified via its IP and port
     */
    private static final Logger LOG = LoggerFactory.getLogger(VispCommunicationController.class);

    @Autowired
    private VispCommunicator vispCommunicator;

    @RequestMapping("/addVispRuntime")
    public String addVispRuntime(@RequestParam(value = "endpoint") String endpoint) {
        vispCommunicator.addVispRuntime(new VispRuntimeIdentifier(endpoint));
        return "ok";
    }

    @RequestMapping("/removeVispRuntime")
    public String removeVispRuntime(@RequestParam(value = "endpoint") String endpoint) {
        vispCommunicator.removeVispRuntime(new VispRuntimeIdentifier(endpoint));
        return "ok";
    }

    @RequestMapping("/getVispRuntimes")
    @ResponseBody
    public List<VispRuntimeIdentifier> getVispRuntimes() {
        return vispCommunicator.getVispRuntimeIdentifiers();
    }
}
