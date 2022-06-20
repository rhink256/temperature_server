FROM nexus.local:8080/rhink/wildfly-postgres

ENV JBOSS_HOME /opt/jboss/wildfly

#fix antlr parsing issues in hibernate @OrderBy annotations
ENV JAVA_OPTS=-DANTLR_USE_DIRECT_CLASS_LOADING=true\ -Dlog4j.debug

ENV DB_HOST=database
ENV DB_PORT=5432
ARG DB_NAME=temp_server_db
ARG DB_USER=postgres
ARG DB_PASS=postgres

USER root
RUN yum -y install wget

#ENV TZ=America/Eastern
#RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
USER jboss

#add admin user
RUN /opt/jboss/wildfly/bin/add-user.sh admin admin --silent

# configure datasource
RUN /bin/sh -c '$JBOSS_HOME/bin/standalone.sh &' && \
    sleep 10 && \
    $JBOSS_HOME/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=TempServerDS:add(driver-name=postgresql-42.2.19.jar,jndi-name=\"java:/TempServerDS\",connection-url=\"jdbc:postgresql://$DB_HOST:$DB_PORT/${DB_NAME}\",user-name=${DB_USER},password=${DB_PASS})"

#install backend
#RUN wget http://nexus.local/repository/temperature/local/temperature/server/0.0.1/server-0.0.1.war --directory-prefix=/opt/jboss/wildfly/standalone/deployments/
COPY build/libs/temperature_server.war /opt/jboss/wildfly/standalone/deployments/

#configure timezone
ENV TZ="US/Eastern"
RUN date

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
