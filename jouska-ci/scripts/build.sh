# $1 values [WINDOWS,LINUX,MAC]
# $2 JDK path (platform should be according to the first parameter)
# $3 Javafx Jmods path (platform should be according to the first parameter)

mvn clean install -Dos="$1" && \
mkdir jouska-client/target/output && \
sh jouska-ci/target/appassembler/bin/jre.sh jouska-client/target/lib "$2" "$3" jouska-client/target/output && \
cp "jouska-client/target/classes/config.properties" "jouska-client/target/output/config.properties"
