package net.knasmueller.pathfinder.domain.impl;

import net.knasmueller.pathfinder.domain.IDataFlowProvider;

import java.util.Map;

public class DataFlowProvider implements IDataFlowProvider {

    Map<String, String> dataFlows;

    public DataFlowProvider(Map<String, String> dataFlows) {
        this.dataFlows = dataFlows;
    }

    @Override
    public Map<String, String> getFlows() {
        return dataFlows;
    }
}
