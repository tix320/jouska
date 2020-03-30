# $1 values [WINDOWS,LINUX,MAC]
# $2 maven post command (now used for properties) (example -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $3 JDK path (platform should be according to the first parameter)
# $4 Javafx Jmods path (platform should be according to the first parameter)

mvn clean install -Dos="$1 $2" && \
mkdir jouska-client/target/output && \
sh jouska-ci/target/appassembler/bin/jre.sh "$1" jouska-client/target/lib "$3" "$4" jouska-client/target/output && \
cp "jouska-client/target/classes/config.properties" "jouska-client/target/output/config.properties"
