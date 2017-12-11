package com.larscheidschmitzhermes.keycloak.events;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class MonitoringEventListenerProviderFactory implements EventListenerProviderFactory {
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
            this.eventsDirectory = eventsDirectoryConfigVal;
        } else {
            this.eventsDirectory = System.getenv(EVENTS_DIRECTORY_ENV);
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
