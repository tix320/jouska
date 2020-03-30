# $1 values [WINDOWS,LINUX,MAC]
# $2 JDK path (platform should be according to the first parameter)
# $3 Javafx Jmods path (platform should be according to the first parameter)

mvn clean install -Dos="$1" && \
sh jouska-ci/target/appassembler/bin/jre.sh jouska-client/target/lib jouska-client/target/output "$2" "$3" && \
cp "jouska-client/target/classes/config.properties" "jouska-client/target/output/config.properties"
