package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VispTopology {
    Map<String, Operator> topology;

    public VispTopology(Map<String, Operator> topology) {
        this.topology = topology;
    }

    public VispTopology() {
    }

    public Map<String, Operator> getTopology() {
        return topology;
    }

    public void setTopology(Map<String, Operator> topology) {
        this.topology = topology;
    }
}
