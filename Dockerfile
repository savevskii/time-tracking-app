FROM docker-registry-mirror.netcetera.com/distroless/java21-debian12

# Set working directory
WORKDIR /app

# [Mandatory] Switch to non-root runtime user
USER nonroot

# Copy the jar file from the builder stage to the runtime stage
COPY --chown=nonroot /time-tracking-application/target/time-tracking-app-exec.jar /app/time-tracking-app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/time-tracking-app.jar"]