# $1 maven post command (now used for properties) (example -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $2 Windows JDK path
# $3 Output folder

sh jouska-ci/build/base/bot-build.sh "$1 -Dos=WINDOWS" "$2" && \
cp -R jouska-ci/build/windows/bot/static/* jouska-bot/target/output && \
cd jouska-bot/target/output && zip -r ../../../"$3"/jouska-bot-windows.zip *
