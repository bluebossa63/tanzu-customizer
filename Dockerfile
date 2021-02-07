FROM openjdk:15
LABEL maintainer="daniele.ulrich@niceneasy.ch"
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV LOG_LEVEL_ROOT=INFO
ENV LOG_LEVEL_org.apache.http=INFO
ENV LOG_LEVEL_org.springframework=INFO  
ENV WINRM_SERVER=<your_server>
ENV WINRM_USERNAME=<your_username> 
ENV WINRM_PASSWORD=<your_password>
ENV DNS_MANAGEMENT_ENABLED=false
ENV KUBECONFIG=/etc/kubernetes/cluster.conf
EXPOSE 8679