# $1 values [WINDOWS,LINUX,MAC]
# $2 JDK path (platform should be according to the first parameter)
# $3 Javafx Jmods path (platform should be according to the first parameter)

mvn clean install -Dos="$1" \
sh jouska-ci/target/appassembler/bin/jre.sh jouska-client/target "$2" "$3" && \
cp "jouska-client/target/classes/config.properties" "jouska-client/target/config.properties" && \
rmdir -r jouska-client/target/classes jouska-client/target/generated-sources jouska-client/target/generated-test-sources jouska-client/target/test-classes \
jouska-client/target/maven-archiver jouska-client/target/maven-status jouska-client/target/lib
