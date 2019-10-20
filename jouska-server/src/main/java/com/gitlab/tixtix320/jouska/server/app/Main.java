package com.gitlab.tixtix320.jouska.server.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.GameBoards;

import static com.gitlab.tixtix320.jouska.server.app.Services.SONDER;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Services.initialize(8888);
//        Thread.sleep(7000);
//        TopicPublisher<GameBoard> publisher = SONDER.registerTopicPublisher("pizdec", new TypeReference<>() {
//        });
//        GameBoard board = GameBoards.defaultBoard(2);
//        publisher.publish(board);
    }
}
