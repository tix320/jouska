package com.gitlab.tixtix320.jouska.client.app;


import com.gitlab.tixtix320.jouska.client.service.GameService;
import com.gitlab.tixtix320.sonder.api.client.Clonder;

import java.io.IOException;

public class Services {
    public static GameService GAME_SERVICE;

    public static Clonder CLONDER;

    public static void initialize(String host, int port) {
        if (CLONDER != null) {
            throw new IllegalStateException("Client already start, maybe you wish reconnect?");
        }
        String servicesPackage = "com.gitlab.tixtix320.jouska.client.service";
        CLONDER = Clonder.withBuiltInProtocols(host, port, servicesPackage);
        initServices();
    }

    public static void stop() throws IOException {
        if (CLONDER == null) {
            throw new IllegalStateException("Client does not started");
        }
        CLONDER.close();
    }

    private static void initServices() {
        GAME_SERVICE = CLONDER.getRPCService(GameService.class);
    }
}
