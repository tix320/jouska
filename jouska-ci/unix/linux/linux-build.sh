# $1 Linux JDK path
# $2 Linux Javafx Jmods path

sh jouska-ci/unix/unix-build.sh LINUX "$1" "$2" && \
cp -R jouska-ci/unix/linux/include/* jouska-client/target/output && \
sh jouska-ci/unix/installer/generate-installer.sh jouska-client/target/output/jouska-linux-setup.run
