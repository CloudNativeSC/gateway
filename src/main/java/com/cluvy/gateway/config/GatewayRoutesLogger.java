package com.cluvy.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GatewayRoutesLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GatewayRoutesLogger.class);
    private final RouteDefinitionLocator routeDefinitionLocator;

    public GatewayRoutesLogger(RouteDefinitionLocator routeDefinitionLocator) {
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("==== Gateway Routes Start ====");
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();
        if (routes != null) {
            for (RouteDefinition route : routes) {
                log.info("Route ID: {}", route.getId());
                log.info("URI: {}", route.getUri());
                log.info("Predicates: {}", route.getPredicates());
                log.info("Filters: {}", route.getFilters());
            }
        } else {
            log.warn("No routes found!");
        }
        log.info("==== Gateway Routes End ====");
    }
}
