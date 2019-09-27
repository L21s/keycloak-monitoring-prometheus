FROM jboss/keycloak:7.0.0
COPY jboss-cli /opt/jboss/startup-scripts/prometheus.cli
RUN /opt/jboss/keycloak/bin/run-cli.sh --file=/opt/jboss/startup-scripts/prometheus.cli
COPY build/libs/*.jar keycloak/providers/
