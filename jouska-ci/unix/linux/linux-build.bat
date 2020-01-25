cmd /c ""jouska-ci/unix/unix-build.bat"" LINUX C:\dev\jdk-linux\jdk-11.0.6 C:\dev\javafx-linux && ^
xcopy "jouska-ci\unix\linux\include" "jouska-client\target" && ^
start "" "%PROGRAMFILES%\Git\bin\sh.exe" jouska-ci/unix/installer/generate-installer.sh jouska-linux.run
