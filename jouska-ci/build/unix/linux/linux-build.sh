# $1 maven post command (now used for properties) (example -Djouska.server.host=1.1.1.1 -Djouska.server.port=8888)
# $2 Linux JDK path
# $3 Linux Javafx Jmods path
# $4 Output folder

sh jouska-ci/build/base/build.sh "$1 -Dos=LINUX" "$2" "$3" && \
cp -R jouska-ci/build/unix/installer/include/* jouska-client/target/output && \
cp -R jouska-ci/build/unix/linux/include/* jouska-client/target/output && \
sh jouska-ci/build/unix/installer/generate-installer.sh jouska-client/target/output "$4"/jouska-linux-setup.sh