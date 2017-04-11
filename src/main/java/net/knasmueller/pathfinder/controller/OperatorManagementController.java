package net.knasmueller.pathfinder.controller;


import net.knasmueller.pathfinder.entities.Operator;
import net.knasmueller.pathfinder.entities.VispRuntimeIdentifier;
import net.knasmueller.pathfinder.service.OperatorManagement;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/operator")
public class OperatorManagementController {

    private static final Logger LOG = LoggerFactory.getLogger(OperatorManagementController.class);

    @Autowired
    OperatorManagement operatorManagement;

    @RequestMapping("/setOperatorStatus")
    public String setOperatorStatus(@RequestParam(value = "operatorId") String operatorId, @RequestParam(value = "status") String status) {
        operatorManagement.setOperatorStatus(operatorId, status);
        return "ok";
    }

    @RequestMapping("/getOperators")
    public ConcurrentHashMap<String, Operator> getOperators() {
        return operatorManagement.getOperators();
    }
}
