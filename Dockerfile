
FROM bellsoft/liberica-openjdk-alpine:17

COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Дефинираме точката на влизане (entrypoint)
# Това е командата, която се изпълнява при стартиране на контейнера
# java $JAVA_OPTS: изпълнява Java с опциите от JAVA_OPTS
# -jar app.jar: стартира Spring Boot приложението от JAR файла
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar","app.jar"]


# ============================================
# Бележки за използване:
# ============================================
# За да създадете Docker образ:
#   docker build -t smart-expense .
#
# За да стартирате контейнера:
#   docker run -p 9090:9090 smart-expense
#
# За да стартирате с MySQL база данни:
#   docker run -p 9090:9090 \
#     -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/SmartExpens \
#     -e SPRING_DATASOURCE_USERNAME=root \
#     -e SPRING_DATASOURCE_PASSWORD=yourpassword \
#     smart-expense:latest
#
# За да стартирате с docker-compose (препоръчително):
#   Създайте docker-compose.yml файл с приложението и MySQL базата данни
