# build, we need jdk
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# run , we need only jre
FROM eclipse-temurin:21-jre

WORKDIR /app

# copy just .jar
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
