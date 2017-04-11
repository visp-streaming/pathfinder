package net.knasmueller.pathfinder.entities;


public class Operator {
    public enum Status {WORKING, FAILED}

    String name;
    Status status;

    public Operator() {
        this.name = "";
        this.status = Status.WORKING;
    }

    public Operator(String name) {
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
