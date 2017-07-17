package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.Operator;
import net.knasmueller.pathfinder.exceptions.EmptyTopologyException;
import net.knasmueller.pathfinder.exceptions.OperatorNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Manages the current visp topology
 * The topology is changed by parsing updated topology strings from VISP regularly
 * The topology is queried for getting details about operators and paths that may be needed for the functioning of pathfinder
 */
@Service
public class VispTopology {
    Map<String, Operator> topology;

    /**
     * Directly initialize topology with a topology-map
     * @param topology
     */
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

    /**
     * Returns split operators (operators that can use alternative fallback paths depending on the online status of
     * the child nodes)
     * @return set of split-operator ids
     * @throws EmptyTopologyException
     */
    public Set<String> getSplitOperatorIds() throws EmptyTopologyException {
        if(this.getTopology() == null || this.getTopology().size() == 0) {
            throw new EmptyTopologyException("Topology is empty");
        }
        return ProcessingOperatorHealth.getAlternativePaths(getTopology()).keySet();
    }

    /**
     * returns the class-representation of a specific operator id
     * @param id string identifier of the operator (as specified in the topology map as the key attribute)
     * @return class representation of the operator
     * @throws EmptyTopologyException
     * @throws OperatorNotFoundException
     */
    public Operator getOperator(String id) throws EmptyTopologyException, OperatorNotFoundException {
        if(this.getTopology() == null || this.getTopology().size() == 0) {
            throw new EmptyTopologyException("Topology is empty");
        } else if(!this.getTopology().containsKey(id)) {
            throw new OperatorNotFoundException("Operator " + id + " is not available");
        } else {
            return this.getTopology().get(id);
        }
    }

    public String getHash() throws EmptyTopologyException {
        if(this.getTopology() == null || this.getTopology().size() == 0) {
            throw new EmptyTopologyException("Topology is empty");
        }
        return "T" + topology.hashCode();
    }
}
