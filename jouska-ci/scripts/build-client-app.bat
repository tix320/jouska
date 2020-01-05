mvn clean install && ^
cmd /c ""jouska-ci/target/appassembler/bin/ci.bat"" jouska-client/target >> jouska-client/target/jouska.bat && ^
cmd /c ""jouska-ci\scripts\generate-launcher.bat"" jouska-client\target\jouska.bat jouska-client\target\Jouska.exe jouska-ci\resources\icon.ico
