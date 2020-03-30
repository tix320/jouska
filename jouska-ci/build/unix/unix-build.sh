# $1 values [LINUX,MAC]
# $2 maven post command (now used for properties) (example -Dos=LINUX -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $3 JDK path (platform should be according to the first parameter)
# $4 Javafx Jmods path (platform should be according to the first parameter)

sh jouska-ci/build/base/build.sh "$2 -Dos=$1" "$3" "$4" && \
cp -R jouska-ci/build/unix/installer/include/* jouska-client/target/output

