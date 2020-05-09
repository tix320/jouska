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

# Guide to real bot process communication

#### Example: client ----> bot , client <---- bot
    START_GAME  ---->
    3 3         ---->
    TURN        ---->
    0,0 0,0 1,1 ---->
    2,1 2,1 1,2 ---->
    1,3 1,2 1,1 ---->
    0:2         <----
    END_GAME    ---->
    START_GAME  ---->
    4 3         ---->
    TURN        ---->
    1,2 2,1 1,1 ---->
    2,1 2,1 1,2 ---->
    1,3 0,0 1,1 ---->
    1,3 0,2 1,1 ---->
    2:2         <----
    END_GAME    ---->
    

