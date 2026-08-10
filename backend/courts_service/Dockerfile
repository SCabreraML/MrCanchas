FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew --no-daemon dependencies

COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jdk AS runtime

WORKDIR /app

# Cambiado a *.jar para evitar fallos si el nombre no termina en -SNAPSHOT.jar
COPY --from=build /app/build/libs/*.jar app.jar

# Cambia el 8686 si este microservicio usa otro puerto en su application.properties
EXPOSE 8686

ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
