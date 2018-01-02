package com.larscheidschmitzhermes.keycloak.events;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class MonitoringEventListenerProviderFactory implements EventListenerProviderFactory {
    private static final Logger logger = Logger.getLogger(MonitoringEventListenerProviderFactory.class);
    private final String EVENTS_DIRECTORY_ENV = "KEYCLOAK_PROMETHEUS_EVENTS_DIR";

    private String eventsDirectory;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new MonitoringEventListenerProvider(eventsDirectory);
    }

    @Override
    public void init(Config.Scope config) {
        String eventsDirectoryConfigVal = config.get("eventsDirectory");
        if (eventsDirectoryConfigVal != null && !eventsDirectoryConfigVal.isEmpty()) {
            logger.debug("Using events directory from configuration file: " + eventsDirectoryConfigVal);
            this.eventsDirectory = eventsDirectoryConfigVal;
        } else {
            String eventsDirectoryEnvVal = System.getenv(EVENTS_DIRECTORY_ENV);
            logger.debug("Using events directory from " + EVENTS_DIRECTORY_ENV + " environment variable: " + eventsDirectoryEnvVal);
            this.eventsDirectory = eventsDirectoryEnvVal;
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        //nothing to do
    }

    @Override
    public void close() {
        //nothing to do
    }

    @Override
    public String getId() {
        return "com.larscheidschmitzhermes:keycloak-monitoring-prometheus";
    }
}
