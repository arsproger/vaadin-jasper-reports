FROM openjdk:17

WORKDIR /app

COPY . /app

EXPOSE 8080

RUN mkdir -p /usr/share/fonts/myfonts

COPY *.ttf /usr/share/fonts/myfonts/

RUN fc-cache -f -v

COPY target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]