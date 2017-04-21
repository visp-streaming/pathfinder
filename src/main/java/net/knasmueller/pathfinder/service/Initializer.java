package net.knasmueller.pathfinder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Initializer {

    private static final Logger LOG = LoggerFactory.getLogger(Initializer.class);

    @PostConstruct
    public void init(){
        LOG.debug("Running initialization procedure");
    }

    // TODO: reconnect to past known instances
}