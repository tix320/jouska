mvn clean install && ^
cmd /c ""jouska-ci/target/appassembler/bin/ci.bat"" jouska-client/target >> jouska-client/target/jouska.bat && ^
copy "jouska-ci\scripts\exe-builder.bat" "jouska-client\target\exe-builder.bat" && ^
copy "jouska-ci\scripts\jouska.vbs" "jouska-client\target\jouska.vbs"
