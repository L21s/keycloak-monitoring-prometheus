package com.larscheidschmitzhermes.keycloak.events;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

public class MonitoringEventListenerProviderTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Event event() {
        Event event = new Event();
            event.setRealmId("test-realm");
            event.setType(EventType.LOGIN);
            return event;
    }

    private AdminEvent adminEvent() {
        AdminEvent event = new AdminEvent();
            event.setRealmId("test-realm");
            event.setOperationType(OperationType.UPDATE);
            event.setResourceType(ResourceType.CLIENT);
            return event;
    }

    @Test
    public void shouldGenerateFilesWithCorrectNamesForNormalEvents() throws IOException {
        MonitoringEventListenerProvider listener = new MonitoringEventListenerProvider(tmp.getRoot().getAbsolutePath());

        listener.onEvent(event());

        File expectedFile = new File(tmp.getRoot().getAbsolutePath() + File.separator + "keycloak_events_total;realm=test-realm;type=LOGIN");

        MatcherAssert.assertThat(expectedFile.exists(), Is.is(true));
        MatcherAssert.assertThat(FileUtils.readFileToString(expectedFile), Is.is("1"));
    }

    @Test
    public void shouldGenerateFilesWithCorrectNamesForAdminEvents() throws IOException {
        MonitoringEventListenerProvider listener = new MonitoringEventListenerProvider(tmp.getRoot().getAbsolutePath());

        listener.onEvent(adminEvent(), false);

        File expectedFile = new File(tmp.getRoot().getAbsolutePath() + File.separator + "keycloak_admin_events_total;realm=test-realm;operation=UPDATE;resource=CLIENT");

        MatcherAssert.assertThat(expectedFile.exists(), Is.is(true));
        MatcherAssert.assertThat(FileUtils.readFileToString(expectedFile), Is.is("1"));
    }

    @Test
    public void shouldProperlyIncreaseCounterForNormalEvents() throws IOException {
        MonitoringEventListenerProvider listener = new MonitoringEventListenerProvider(tmp.getRoot().getAbsolutePath());
        File existingCounter = new File(tmp.getRoot().getAbsolutePath() + File.separator + "keycloak_events_total;realm=test-realm;type=LOGIN");
        FileUtils.writeStringToFile(existingCounter, "100");

        listener.onEvent(event());

        MatcherAssert.assertThat(FileUtils.readFileToString(existingCounter), Is.is("101"));
    }

    @Test
    public void shouldProperlyIncreaseCounterForAdminEvents() throws IOException {
        MonitoringEventListenerProvider listener = new MonitoringEventListenerProvider(tmp.getRoot().getAbsolutePath());
        File existingCounter = new File(tmp.getRoot().getAbsolutePath() + File.separator + "keycloak_admin_events_total;realm=test-realm;operation=UPDATE;resource=CLIENT");
        FileUtils.writeStringToFile(existingCounter, "100");

        listener.onEvent(adminEvent(), false);

        MatcherAssert.assertThat(FileUtils.readFileToString(existingCounter), Is.is("101"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldPropagateAnyFileRelatedErrors() {
        MonitoringEventListenerProvider listener = new MonitoringEventListenerProvider(tmp.getRoot().getAbsolutePath() + File.separator + "non-existent");

        listener.onEvent(event());
    }
}
