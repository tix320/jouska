REM: %1 maven post command (now used for properties) (example -Dos=LINUX -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
REM: %2 JDK path (platform should be according to the first parameter)
REM: %3 Javafx Jmods path (platform should be according to the first parameter)

mvn clean install %1 && ^
mkdir jouska-client\target\output && ^
cmd /c ""jouska-ci/target/appassembler/bin/jre.bat"" %1 jouska-client/target/lib %2 %3 jouska-client/target/output && ^
copy "jouska-client\target\classes\config.properties" "jouska-client\target\output\config.properties"
