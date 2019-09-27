FROM jboss/keycloak:7.0.0
COPY jboss-cli /opt/jboss/tools/prometheus.cli
RUN /opt/jboss/keycloak/bin/jboss-cli.sh --file=/opt/jboss/tools/prometheus.cli
COPY build/libs/*.jar keycloak/providers/
