package net.knasmueller.pathfinder.entities;


import ac.at.tuwien.infosys.visp.common.operators.*;

import java.util.ArrayList;

/**
 * Extension of the operator for PathFinder specific purposes
 */
public class PathfinderOperator extends Operator {

    public PathfinderOperator(Operator operator) {
        this.name = new String(operator.getName());
        this.sources = new ArrayList<>(operator.getSources());
        this.affectedInstances = new ArrayList<>(operator.getAffectedInstances());
        this.allowedLocationsList = new ArrayList<>(operator.getAllowedLocationsList());
        this.concreteLocation = operator.getConcreteLocation();
        this.stateful = operator.isStateful();
        this.size = operator.getSize();
        this.outputFormat = operator.getOutputFormat() == null ? new String("") : new String(operator.getOutputFormat());
        this.status = Status.WORKING;
        this.sourcesText = new ArrayList<>(operator.getSourcesText());
        this.type = operator.getType() == null ? new String("") : new String(operator.getType());
        this.inputFormat = operator.getInputFormat() != null ? (new ArrayList<>(operator.getInputFormat())) : new ArrayList<>();

        if(operator instanceof Source) {
            this.subclass = Subclass.SOURCE;
        } else if(operator instanceof Sink) {
            this.subclass = Subclass.SINK;
        } else if(operator instanceof ProcessingOperator) {
            this.subclass = Subclass.PROCESSING;
        } else if(operator instanceof Split) {
            this.subclass = Subclass.SPLIT;
        } else if(operator instanceof Join) {
            this.subclass = Subclass.JOIN;
        } else {
            this.subclass = Subclass.NONE;
        }
    }



    public enum Subclass {SOURCE, SINK, PROCESSING, SPLIT, JOIN, NONE}

    public enum Status {WORKING, FAILED}

    Status status;

    Subclass subclass;

    public PathfinderOperator() {
        this.name = "";
        this.status = Status.WORKING;
        this.subclass = Subclass.NONE;
    }

    public PathfinderOperator(String name) {
        this.name = name;
        this.status = Status.WORKING;
        this.subclass = Subclass.NONE;
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

    /**
     * Returns enum describing the original operator's subclass
     * @return
     */
    public Subclass getSubclass() {
        return subclass;
    }

    public void setSubclass(Subclass subclass) {
        this.subclass = subclass;
    }
}
