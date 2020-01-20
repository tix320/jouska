mvn clean install -Dos=%1 && ^
cmd /c ""jouska-ci/target/appassembler/bin/jre.bat"" jouska-client/target && ^
copy "jouska-client\target\classes\config.properties" "jouska-client\target\config.properties" && ^
rmdir /S /Q jouska-client\target\classes jouska-client\target\generated-sources jouska-client\target\maven-archiver jouska-client\target\maven-status jouska-client\target\lib
