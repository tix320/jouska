REM: %1 maven post command (now used for properties) (example -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
REM: %2 Mac JDK path
REM: %3 Mac Javafx Jmods path

cmd /c ""jouska-ci/build/unix/unix-build.bat"" MAC %1 %2 %3 && ^
xcopy "jouska-ci\build\unix\mac\include" "jouska-client\target\output" && ^
start "" "%PROGRAMFILES%\Git\bin\sh.exe" jouska-ci/build/unix/installer/generate-installer.sh jouska-client/target/output jouska-client/target/output/jouska-mac-setup.sh
