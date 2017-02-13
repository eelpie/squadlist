FROM openjdk:8-jre
COPY target/squadlist-0.0.1-SNAPSHOT.jar /opt/squadlist/squadlist-0.0.1-SNAPSHOT.jar
CMD ["java","-jar","/opt/squadlist/squadlist-0.0.1-SNAPSHOT.jar", "--spring.config.location=/opt/squadlist/conf/squadlist.properties"]