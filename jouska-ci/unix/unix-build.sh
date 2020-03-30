# $1 values [WINDOWS,LINUX,MAC]
# $2 JDK path (platform should be according to the first parameter)
# $3 Javafx Jmods path (platform should be according to the first parameter)

sh jouska-ci/scripts/build.sh "$1" "$2" "$3" && \
cp -R jouska-ci/unix/installer/include/ jouska-client/target/output
