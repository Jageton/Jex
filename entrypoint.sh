#!/bin/sh -x
set -e

exec java -jar -Dbot.token=${TELEGRAM_BOT_TOKEN} -Dbot.username=${TELEGRAM_BOT_USERNAME} -Dbot.payment-token=${TELEGRAM_BOT_PAYMENT_TOKEN} telegram-bot.jar