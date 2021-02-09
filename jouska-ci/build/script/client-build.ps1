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
--launcher runner="jouska.client/com.github.tix320.jouska.client.app.Main" `
--module-path "$JDKJmodsPath;$JavaFxJmodsPath;jouska-client/target/app.jar;jouska-client/target/lib/" `
--add-modules "jouska.client" `
--add-options="-Djouska.server.host=$serverHost -Djouska.server.port=$serverPort" `
--output "jouska-client/target/output"