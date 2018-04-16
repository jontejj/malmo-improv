FROM jetty:alpine
#WORKDIR /app
ADD reservations-simple-1.0-SNAPSHOT.war /var/lib/jetty/webapps
#COPY target/reservations-simple-1.0-SNAPSHOT.war /usr/reservations.war
#RUN wget "http://central.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.9.v20180320/jetty-distribution-9.4.9.v20180320.zip"
#WORKDIR /usr/src/
EXPOSE 8080
CMD java -jar $JETTY_HOME/start.jar