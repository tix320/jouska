# $1 Linux JDK path
# $2 Linux Javafx Jmods path

sh jouska-ci/unix/unix-build.sh LINUX "$1" "$2" && \
cp -R jouska-ci/unix/linux/include jouska-client/target/ && \
sh jouska-ci/unix/installer/generate-installer.sh jouska-linux-setup.run