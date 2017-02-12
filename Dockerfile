FROM openjdk:8-jre
COPY target/squadlist-0.0.1-SNAPSHOT.jar /opt/squadlist/squadlist-0.0.1-SNAPSHOT.jar
COPY *.cer /tmp/
COPY *.crt /tmp/
CMD ["USER", "root"]
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -alias startcom.ca -file /tmp/ca.cer
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -alias startcom.ca.sub.class1 -file /tmp/sub.class1.server.ca.crt
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -alias startcom.ca.sub.class2 -file /tmp/sub.class2.server.ca.crt
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -alias startcom.ca.sub.class3 -file /tmp/sub.class3.server.ca.crt
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -alias startcom.ca.sub.class4 -file /tmp/sub.class4.server.ca.crt
RUN rm /tmp/*.crt
CMD ["java","-jar","/opt/squadlist/squadlist-0.0.1-SNAPSHOT.jar", "--spring.config.location=/opt/squadlist/conf/squadlist.properties"]
