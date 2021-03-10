#Build Guide

##Example

```
mvn clean package 
"-Djouska.server.host=localhost" 
"-Djouska.server.port=8888" 
"-Dwindows-jdk-jmods-path=/home/dev/windows-jdk-16/jmods"
"-Dlinux-jdk-jmods-path=/home/dev/linux-jdk-16/jmods"
"-Dmacos-jdk-jmods-path=/home/dev/macos-jdk-16/jmods"
"-Dwindows-javafx-jmods-path=/home/dev/windows-javafx-jmods-15.0.1" 
"-Dlinux-javafx-jmods-path=/home/dev/linux-javafx-jmods-15.0.1" 
"-Dmac-javafx-jmods-path=/home/dev/mac-javafx-jmods-15.0.1" 
```