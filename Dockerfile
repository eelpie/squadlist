FROM eclipse-temurin:17-alpine
COPY target/squadlist-0.0.1-SNAPSHOT.jar /opt/squadlist/squadlist-0.0.1-SNAPSHOT.jar

COPY honeycomb/honeycomb-opentelemetry-javaagent-1.3.0.jar /opt/honeycomb-opentelemetry-javaagent-1.3.0.jar

CMD ["java", "-XshowSettings:vm", "-XX:+PrintCommandLineFlags", "-XX:MaxRAMPercentage=75", "-javaagent:/opt/honeycomb-opentelemetry-javaagent-1.3.0.jar", "-jar", "/opt/squadlist/squadlist-0.0.1-SNAPSHOT.jar", "--spring.config.location=/opt/squadlist/conf/squadlist.properties"]


