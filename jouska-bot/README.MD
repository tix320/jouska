#How to run bot client
####Run it via "jouska-bot.bat" / "jouska-bot.sh" by passing following arguments
1. Real bot process command: "My c++ application run command"
2. Username: MyBot 
3. Password: MyPassword
####Example: sh jouska-bot.sh "java -jar my-real-bot.jar" Bot1 foo

# Guide to CLI
## Available Commands
#### join [params]
##### used params : --game, --tournament
##### example: join --game 456323
##### example: join --tournament 456323

# Guide to real bot process communication via I/O 

#### Example: client ----> bot , client <---- bot
    START_GAME  ---->
    3 3         ----> board matrix size
    TURN        ---->
    0,0 0,0 1,1 ----> board, each cell like 1,2 
    2,1 2,1 1,2 ----> first number in cell is indicated domination, 0 - nobody, 1-me, 2-opponent
    1,3 1,2 1,1 ----> second number in cell indicated points of that cell
    0:2         <----
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
    

