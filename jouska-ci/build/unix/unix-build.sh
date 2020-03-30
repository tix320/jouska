# $1 maven post command (now used for properties) (example -Dos=LINUX -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $2 JDK path (platform should be according to the first parameter)
# $3 Javafx Jmods path (platform should be according to the first parameter)

sh jouska-ci/build/base/build.sh "$1" "$2" "$3" && \
cp -R jouska-ci/build/unix/installer/include/* jouska-client/target/output &&
