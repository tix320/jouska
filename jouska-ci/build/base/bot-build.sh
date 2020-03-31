# $1 maven post command (now used for properties) (example -Dos=LINUX -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $2 JDK path (platform should be according to the first parameter)

mvn clean install $1 && \
mkdir jouska-bot/target/output && \
sh jouska-ci/target/appassembler/bin/bot-jre.sh unix jouska-bot/target/lib "$2" jouska-bot/target/output && \
cp jouska-bot/target/classes/config.properties jouska-bot/target/output/config.properties && \
cp jouska-bot/target/jouska-bot.jar jouska-bot/target/output/jouska-bot.jar
