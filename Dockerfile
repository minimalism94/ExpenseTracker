    # ============================================
    # Първи етап: Сграждане на приложението
    # ============================================
    # Използваме официалния Maven образ с Java 17 като базов образ
    # Този образ съдържа Maven и Java, необходими за компилиране на проекта
    FROM maven:3.9-eclipse-temurin-17 AS build

    # Задаваме работната директория в контейнера
    # Всички команди ще се изпълняват от тази директория
    WORKDIR /app

    # Копираме pom.xml файла в контейнера
    # Това се прави отделно, за да се използва Docker кешът
    # Ако pom.xml не се е променил, Maven зависимостите няма да се изтеглят отново
    COPY pom.xml .

    # Изтегляме всички Maven зависимости
    # Това се изпълнява преди копирането на изходния код за по-добро кеширане
    RUN mvn dependency:go-offline -B

    # Копираме целия изходен код в контейнера
    COPY src ./src

    # Компилираме приложението и създаваме JAR файла
    # -B означава batch mode (без интерактивен режим)
    # -DskipTests пропуска тестовете при компилиране (може да ги включите, ако искате)
    RUN mvn clean package -B -DskipTests

    # ============================================
    # Втори етап: Създаване на финалния образ
    # ============================================
    # Използваме официалния OpenJDK 17 образ като базов
    # Използваме eclipse-temurin, който е официалния OpenJDK дистрибутор
    FROM eclipse-temurin:17-jre-jammy

    # Добавяме метаданни към образа (незадължително, но полезно)
    LABEL maintainer="SmartExpense Team"
    LABEL description="Smart Expense Tracker Application"

    # Създаваме група и потребител за приложението
    # Това е добра практика за сигурност - не изпълняваме приложението като root
    RUN groupadd -r spring && useradd -r -g spring spring

    # Задаваме работната директория
    WORKDIR /app

    # Копираме компилирания JAR файл от build етапа
    # Името на файла може да варира, но обикновено е в target/ директорията
    # Използваме wildcard за да намерим JAR файла независимо от версията
    COPY --from=build /app/target/demo-*.jar app.jar

    # Променяме собственика на файла на spring потребителя
    # Това гарантира, че приложението има правилните права за достъп
    RUN chown spring:spring app.jar

    # Преминаваме на spring потребителя
    # От сега нататък всички команди се изпълняват като този потребител
    USER spring

    # Отваряме порта, на който приложението слуша
    # По подразбиране Spring Boot приложенията слушат на порт 8080
    # Но в application.properties е зададен порт 9090
    EXPOSE 9090

    # Задаваме променливи на средата за Java
    # -Xmx512m: максимална памет за heap (512MB)
    # -Xms256m: начална памет за heap (256MB)
    # Можете да промените тези стойности според нуждите на приложението
    ENV JAVA_OPTS="-Xmx512m -Xms256m"

    # Дефинираме точката на влизане (entrypoint)
    # Това е командата, която се изпълнява при стартиране на контейнера
    # java $JAVA_OPTS: изпълнява Java с опциите от JAVA_OPTS
    # -Dspring.profiles.active=prod: задава Spring профила на "prod" (production)
    # -jar app.jar: стартира Spring Boot приложението от JAR файла
    ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]

    # ============================================
    # Бележки за използване:
    # ============================================
    # За да създадете Docker образ:
    #   docker build -t smart-expense:latest .
    #
    # За да стартирате контейнера:
    #   docker run -p 9090:9090 smart-expense:latest
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