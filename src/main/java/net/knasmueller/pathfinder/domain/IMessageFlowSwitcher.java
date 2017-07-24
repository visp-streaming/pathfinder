package net.knasmueller.pathfinder.domain;

public interface IMessageFlowSwitcher {
    void stopMessageFlow(String parentSplit, String op);

    void resumeMessageFlow(String parentSplit, String bestAvailablePath);

    void probeMessageFlow(String parentSplitOperator, String op);
}
