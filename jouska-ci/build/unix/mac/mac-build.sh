# $1 maven post command (now used for properties) (example -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $2 Linux JDK path
# $3 Linux Javafx Jmods path

sh jouska-ci/build/unix/unix-build.sh -Dos=MAC "$1" "$2" "$3" && \
cp -R jouska-ci/build/unix/mac/include/* jouska-client/target/output && \
sh jouska-ci/build/unix/installer/generate-installer.sh jouska-client/target/output jouska-client/target/output/jouska-mac-setup.sh
