# $1 maven post command (now used for properties) (example -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $2 Windows JDK path
# $3 Windows Javafx Jmods path
# $4 Output folder

sh jouska-ci/build/base/build.sh WINDOWS "$1" "$2" "$3" && \
xcopy "jouska-ci\build\windows\installer\include" "jouska-client\target\output" && \
zip -r "$4"/jouska-windows.zip jouska-client/target/output/
