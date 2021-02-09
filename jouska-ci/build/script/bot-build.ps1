# $1 JDK jmods path for image
# $2 JavaFX jmods path for image
# $3 Server host
# $4 Server port

echo "--Arguments--"
echo $args
echo "----------"

$JDKJmodsPath = $args[0]
$JavaFxJmodsPath = $args[1]
$serverHost = $args[2]
$serverPort = $args[3]

mvn clean install

jlink --compress 1 --no-header-files --no-man-pages --strip-debug `
--launcher runner="jouska.bot/com.github.tix320.jouska.bot.BotApp" `
--module-path "$JDKJmodsPath;$JavaFxJmodsPath;jouska-bot/target/app.jar;jouska-bot/target/lib/" `
--add-modules "jouska.bot" `
--add-options="-Djouska.server.host=$serverHost -Djouska.server.port=$serverPort" `
--output "jouska-bot/target/output"