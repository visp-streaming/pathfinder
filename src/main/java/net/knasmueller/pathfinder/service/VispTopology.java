package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.exceptions.OperatorNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Set<String> getSplitOperatorIds() throws EmptyTopologyException {
        if(this.getTopology() == null || this.getTopology().size() == 0) {
            throw new EmptyTopologyException("Topology is empty");
        }
        return ProcessingOperatorManagement.getAlternativePaths(getTopology()).keySet();
    }

    public Operator getOperator(String id) throws EmptyTopologyException, OperatorNotFoundException {
        if(this.getTopology() == null || this.getTopology().size() == 0) {
            throw new EmptyTopologyException("Topology is empty");
        } else if(!this.getTopology().containsKey(id)) {
            throw new OperatorNotFoundException("Operator " + id + " is not available");
        } else {
            return this.getTopology().get(id);
        }
    }
}
