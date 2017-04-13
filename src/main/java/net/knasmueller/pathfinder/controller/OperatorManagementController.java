package net.knasmueller.pathfinder.controller;


import net.knasmueller.pathfinder.entities.PathfinderOperator;
import net.knasmueller.pathfinder.service.OperatorManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ConcurrentHashMap<String, PathfinderOperator> getOperators() {
        return operatorManagement.getOperators();
    }
}
