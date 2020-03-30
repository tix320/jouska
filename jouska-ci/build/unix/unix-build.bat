REM: %1 maven post command (now used for properties) (example -Dos=LINUX -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
REM: %2 JDK path (platform should be according to the first parameter)
REM: %3 Javafx Jmods path (platform should be according to the first parameter)

cmd /c ""jouska-ci/build/base/build.bat"" %1 %2 %3 && ^
xcopy "jouska-ci\build\unix\installer\include" "jouska-client\target\output"
