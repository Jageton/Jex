FROM openjdk:alpine
MAINTAINER Roma Gordeev <roma.gordeev@gmail.com>

ENV TELEGRAM_BOT_VERSION 0.1-SNAPSHOT

WORKDIR /usr/share/telegram-bot/

# set up docker entrypoint script
COPY entrypoint.sh /usr/share/telegram-bot/entrypoint.sh
RUN chmod +x /usr/share/telegram-bot/entrypoint.sh

COPY build/libs/telegram-bot-${TELEGRAM_BOT_VERSION}.jar /usr/share/telegram-bot/telegram-bot.jar

ENTRYPOINT ["/usr/share/telegram-bot/entrypoint.sh"]