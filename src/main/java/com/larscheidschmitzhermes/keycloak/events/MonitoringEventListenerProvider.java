package com.larscheidschmitzhermes.keycloak.events;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

public class MonitoringEventListenerProvider implements EventListenerProvider {
    private static final Logger logger = Logger.getLogger(MonitoringEventListenerProvider.class);
    // metrics should be used with this: https://github.com/larscheid-schmitzhermes/prometheus-filesystem-exporter#usage
    // therefore names must look like this: metric_name;label=value;label=value
    private final char DELIMITER = ';';
    private final char LABEL_VALUE_DELIMITER = '=';
    private final String REALM = "realm";
    private final String CLIENT_ID = "client_id";
    private final String IP_ADDRESS = "ip_address";
    private final String TYPE = "type";
    private final String OPERATION = "operation";
    private final String RESOURCE = "resource";

    private String eventsDirectory;

    public MonitoringEventListenerProvider(String eventsDirectory) {
        this.eventsDirectory = eventsDirectory;
    }

    @Override
    public void onEvent(Event event) {
        increaseCounter(getOrCreateCounterFile(generateMetricName(event)));
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        increaseCounter(getOrCreateCounterFile(generateMetricName(event)));
    }

    private String generateMetricName(Event event) {
        StringBuilder sb = new StringBuilder();
        sb.append("keycloak_events_total");
        sb.append(DELIMITER);
        sb.append(generateLabel(REALM, event.getRealmId()));
        sb.append(DELIMITER);
        sb.append(generateLabel(CLIENT_ID, event.getClientId()));
        sb.append(DELIMITER);
        sb.append(generateLabel(IP_ADDRESS, event.getIpAddress()));
        sb.append(DELIMITER);
        sb.append(generateLabel(TYPE, event.getType().toString()));
        return sb.toString();
    }

    private String generateMetricName(AdminEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("keycloak_admin_events_total");
        sb.append(DELIMITER);
        sb.append(generateLabel(REALM, event.getRealmId()));
        sb.append(DELIMITER);
        sb.append(generateLabel(CLIENT_ID, event.getAuthDetails().getClientId()));
        sb.append(DELIMITER);
        sb.append(generateLabel(IP_ADDRESS, event.getAuthDetails().getIpAddress()));
        sb.append(DELIMITER);
        sb.append(generateLabel(OPERATION, event.getOperationType().toString()));
        sb.append(DELIMITER);
        sb.append(generateLabel(RESOURCE, event.getResourceType().toString()));
        return sb.toString();
    }

    private String generateLabel(String name, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(LABEL_VALUE_DELIMITER);
        sb.append(value);
        return sb.toString();
    }

    private synchronized File getOrCreateCounterFile(String fileName) {
        File f = new File(eventsDirectory + File.separator + fileName);
        if (!f.exists()) {
            try {
                logger.debug("File for name: " + fileName + "does not exist, creating");
                f.createNewFile();
                Files.write(f.toPath(), "0".getBytes(Charset.forName("UTF-8")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return f;
    }

    private void increaseCounter(File counterFile) {
        try {
            Long count = Long.parseLong(Files.readAllLines(counterFile.toPath()).get(0));
            count++;
            Files.write(counterFile.toPath(), count.toString().getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        //nothing to close
    }
}
