# $1 maven post command (now used for properties) (example -Dos=LINUX -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $2 JDK path (platform should be according to the first parameter)
# $3 Javafx Jmods path (platform should be according to the first parameter)

mvn clean install -B "$1" && \
mkdir jouska-client/target/output && \
sh jouska-ci/target/appassembler/bin/jre.sh unix jouska-client/target/lib "$3" "$4" jouska-client/target/output && \
cp "jouska-client/target/classes/config.properties" "jouska-client/target/output/config.properties"
