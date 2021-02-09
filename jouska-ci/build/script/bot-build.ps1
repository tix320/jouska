# $1 JDK jmods path for image
# $2 Server host
# $3 Server port

echo "--Arguments--"
echo $args
echo "----------"

$JDKJmodsPath = $args[0]
$serverHost = $args[1]
$serverPort = $args[2]

mvn clean install

jlink --compress 1 --no-header-files --no-man-pages --strip-debug `
--launcher runner="jouska.bot/com.github.tix320.jouska.bot.BotApp" `
--module-path "$JDKJmodsPath;jouska-bot/target/app.jar;jouska-bot/target/lib/" `
--add-modules "jouska.bot" `
--add-options="-Djouska.server.host=$serverHost -Djouska.server.port=$serverPort" `
--output "jouska-bot/target/image"

$compress = @{
    Path = "jouska-bot/target/image"
    CompressionLevel = "Fastest"
    DestinationPath = "jouska-bot/target/image.zip"
}