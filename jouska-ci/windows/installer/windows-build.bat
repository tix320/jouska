cmd /c ""jouska-ci/scripts/build.bat"" WINDOWS && ^
cmd /c ""jouska-ci/windows/installer/generate-launcher.bat"" jouska-ci\windows\installer\include\jouska.bat jouska-client\target\Jouska.exe jouska-ci\resources\icon.ico && ^
cd jouska-client\target && "..\..\jouska-ci/windows/installer/zip" -r jouska.zip .\ && cd ..\..
