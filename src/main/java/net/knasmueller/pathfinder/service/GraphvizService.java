package net.knasmueller.pathfinder.service;

import ac.at.tuwien.infosys.visp.common.operators.*;
import net.knasmueller.pathfinder.exceptions.InvalidCircuitBreakerTransition;
import net.knasmueller.pathfinder.exceptions.UnknownOperatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class GraphvizService {

    @Autowired
    ProcessingOperatorHealth poh;

    public GraphvizService() {

    }

    public String getDotFormatForFrontend(VispTopology vispTopology) {
        String resultString = "digraph {";
        Map<String, Operator> topologyMap = vispTopology.topology;

        if(topologyMap == null) {
            return "";
        }

        for (String operatorId : topologyMap.keySet()) {
            for (Operator source : topologyMap.get(operatorId).getSources()) {
                resultString += source.getName() + " -> " + operatorId + ";\n";
            }
        }

        for (String operatorId : topologyMap.keySet()) {
            resultString += "\"" + operatorId + "\"" + "[style=filled, fontname=\"helvetica\", shape=box, fillcolor=\"" + getColor(operatorId, topologyMap) + "\", label=<<FONT POINT-SIZE=\"12\">" + operatorId + "</FONT><BR />\n" +
                    "<FONT POINT-SIZE=\"8\">" + getSubtitle(operatorId, topologyMap) + "</FONT>>]" + "\n";
        }

        resultString += "\n}";
        return resultString;
    }

    private String getColor(String operatorId, Map<String, Operator> topologyMap) {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 2);
        Operator operator = topologyMap.get(operatorId);
        String result = "";
        if (operator instanceof Split) {
            result = "beige";
        } else if (operator instanceof Join) {
            result = "beige";
        } else if (operator instanceof ProcessingOperator) {
            if(poh.isOperatorAvailable(operatorId)) {
                result = "#5cb85c";
            } else {
                result = "#d9534f";
            }
        } else if (operator instanceof Source) {
            result = "beige";
        } else if (operator instanceof Sink) {
            result = "beige";
        } else {
            result = "unknown";
        }



        return result;
    }

    private String getSubtitle(String operatorId, Map<String, Operator> topologyMap) {
        Operator operator = topologyMap.get(operatorId);

        String result = "";

        if (operator instanceof Split) {
            result = "split";
        } else if (operator instanceof Join) {
            result = "join";
        } else if (operator instanceof ProcessingOperator) {
            result = topologyMap.get(operatorId).getConcreteLocation().toString();
        } else if (operator instanceof Source) {
            result = "source";
        } else if (operator instanceof Sink) {
            result = "sink";
        } else {
            result = "unknown";
        }

        return result;
    }

}
