cmd /c ""jouska-ci/scripts/build.bat"" %1 %2 && ^
xcopy "jouska-ci\unix\installer\include" "jouska-client\target" && ^
start "" "%PROGRAMFILES%\Git\bin\sh.exe" jouska-ci/unix/installer/generate-installer.sh
