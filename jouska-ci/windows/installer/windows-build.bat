cmd /c ""jouska-ci/scripts/build.bat"" WINDOWS C:\dev\jdk-11.0.5 C:\dev\javafx-windows && ^
cmd /c ""jouska-ci/windows/installer/generate-launcher.bat"" jouska-ci\windows\installer\include\jouska.bat jouska-client\target\Jouska.exe jouska-ci\resources\icon.ico && ^
cd jouska-client\target && "..\..\jouska-ci/windows/installer/zip" -r jouska-windows.zip .\ && cd ..\.. && ^
xcopy "jouska-ci\windows\installer\include" "jouska-client\target"