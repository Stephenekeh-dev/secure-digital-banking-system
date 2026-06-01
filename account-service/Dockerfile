FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/account-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", \
  "-Xms50m", "-Xmx100m", \
  "-XX:+UseContainerSupport", \
  "-XX:+UseG1GC", \
  "-jar", "app.jar"]