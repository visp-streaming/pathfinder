package net.knasmueller.pathfinder.domain.impl;

import net.knasmueller.pathfinder.domain.IMessageFlowSwitcher;
import net.knasmueller.pathfinder.service.VispCommunicator;
import org.springframework.beans.factory.annotation.Autowired;

public class VispMessageFlowSwitcher implements IMessageFlowSwitcher {

    VispCommunicator vispCommunicator;

    public VispMessageFlowSwitcher(VispCommunicator vispCommunicator) {
        this.vispCommunicator = vispCommunicator;
    }

    @Override
    public void stopMessageFlow(String parentSplit, String op) {
        vispCommunicator.stopMessageFlow(parentSplit, op);
    }

    @Override
    public void resumeMessageFlow(String parentSplit, String bestAvailablePath) {
        vispCommunicator.resumeMessageFlow(parentSplit, bestAvailablePath);
    }

    @Override
    public void probeMessageFlow(String parentSplitOperator, String op) {
        vispCommunicator.probeMessageFlow(parentSplitOperator, op);
    }
}
