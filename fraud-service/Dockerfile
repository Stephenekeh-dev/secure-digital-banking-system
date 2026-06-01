FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/fraud-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", \
  "-Xms30m", "-Xmx60m", \
  "-XX:+UseContainerSupport", \
  "-XX:+UseG1GC", \
  "-jar", "app.jar"]