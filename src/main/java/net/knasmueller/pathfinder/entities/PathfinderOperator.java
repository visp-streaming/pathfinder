package net.knasmueller.pathfinder.entities;


import ac.at.tuwien.infosys.visp.common.operators.Operator;

public class PathfinderOperator extends Operator {
    public enum Status {WORKING, FAILED}

    Status status;

    public PathfinderOperator() {
        this.name = "";
        this.status = Status.WORKING;
    }

    public PathfinderOperator(String name) {
        this.name = name;
        this.status = Status.WORKING;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
