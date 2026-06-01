FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/transaction-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", \
  "-Xms40m", "-Xmx80m", \
  "-XX:+UseContainerSupport", \
  "-XX:+UseG1GC", \
  "-jar", "app.jar"]