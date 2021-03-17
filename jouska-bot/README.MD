# Build Guide

## Run mvn build
```
mvn clean package -Pprod
"-Djouska.server.host=192.165.113.144" 
"-Djouska.server.port=8888" 
"-Dwindows-jdk-jmods-path=/home/dev/windows-jdk-16/jmods"
"-Dlinux-jdk-jmods-path=/home/dev/linux-jdk-16/jmods"
"-Dmacos-jdk-jmods-path=/home/dev/macos-jdk-16/jmods"
```

# How to run bot client
#### Run it via "runner.bat" / "runner.sh" by passing following arguments
    1. Real bot process command: "My c++ application run command"
    2. Username: MyBot 
    3. Password: MyPassword
#### Example: 
    ./runner "java -jar my-real-bot.jar" Bot1 foo

# Guide to CLI
## Available Commands
    join-game [params]
    join-tournament [parms]
##### example:
    join-game --id 15
    join-tournament --id 32187

# Guide to real bot process communication via I/O 

#### Example: client ----> bot , client <---- bot
    START_GAME  ---->
    3 3         ----> board matrix size
    TURN        ---->
    0,0 0,0 1,1 ----> board, each cell like 1,2 
    2,1 2,1 1,2 ----> first number in cell is indicated domination, 0 - nobody, 1-me, 2-opponent
    1,3 1,2 1,1 ----> second number in cell indicated points of that cell
    0:2         <---- x:y turn coordiantes
    ............----> Simiral turns
    END_GAME    ---->
    START_GAME  ----> Run again
    4 3         ---->
    TURN        ---->
    1,2 2,1 1,1 ---->
    2,1 2,1 1,2 ---->
    1,3 0,0 1,1 ---->
    1,3 0,2 1,1 ---->
    2:2         <----
    END_GAME    ---->
    

