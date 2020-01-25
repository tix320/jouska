cmd /c ""jouska-ci/scripts/build.bat"" %1 %2 %3 && ^
xcopy "jouska-ci\unix\installer\include" "jouska-client\target"
