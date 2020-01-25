cmd /c ""jouska-ci/unix/unix-build.bat"" MAC C:\dev\jdk-macos\Contents\Home C:\dev\javafx-macos && ^
xcopy "jouska-ci\unix\mac\include" "jouska-client\target" && ^
start "" "%PROGRAMFILES%\Git\bin\sh.exe" jouska-ci/unix/installer/generate-installer.sh jouska-mac.run
