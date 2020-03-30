REM: %1 maven post command (now used for properties) (example -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
REM: %2 Windows JDK path
REM: %3 Windows Javafx Jmods path

cmd /c ""jouska-ci/build/base/build.bat"" 'WINDOWS' "%~1" "%~2" "%~3" && ^
xcopy "jouska-ci\build\windows\installer\include" "jouska-client\target\output" && ^
cmd /c ""jouska-ci/build/windows/installer/generate-launcher.bat"" jouska-ci\build\windows\installer\include\jouska.bat jouska-client\target\Jouska.exe jouska-ci\build\windows\resources\icon.ico && ^
cd jouska-client\target\output && "../../../jouska-ci/windows/installer/zip" -r jouska-windows.zip
