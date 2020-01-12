mvn clean install && ^
cmd /c ""jouska-ci/target/appassembler/bin/ci.bat"" jouska-client/target >> jouska-client/target/jouska.bat && ^
cmd /c ""jouska-ci\scripts\generate-launcher.bat"" jouska-client\target\jouska.bat jouska-client\target\Jouska.exe jouska-ci\resources\icon.ico && ^
copy "jouska-client\target\classes\config.properties" "jouska-client\target\config.properties" && ^
rmdir /S /Q jouska-client\target\classes jouska-client\target\generated-sources jouska-client\target\maven-archiver jouska-client\target\maven-status jouska-client\target\lib && ^
cd jouska-client\target && "..\..\jouska-ci/scripts/zip" -r jouska.zip .\ && cd ..\.. && ^
cmd /c ""jouska-ci/target/appassembler/bin/upload.bat"" jouska.zip

