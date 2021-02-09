# $1 JDK jmods path for image
# $2 Server host
# $3 Server port

echo "--Arguments--"
echo "$@"
echo "----------"

JDKJmodsPath=$1
serverHost=$2
serverPort=$3

mvn clean install

jlink --compress 1 --no-header-files --no-man-pages --strip-debug \
--launcher runner="jouska.bot/com.github.tix320.jouska.bot.BotApp" \
--module-path "$JDKJmodsPath:jouska-bot/target/app.jar:jouska-bot/target/lib/" \
--add-modules "jouska.bot" \
--add-options="-Djouska.server.host=$serverHost -Djouska.server.port=$serverPort" \
--output "jouska-bot/target/image"

cd jouska-bot/target && zip -r image.zip image && cd ../..