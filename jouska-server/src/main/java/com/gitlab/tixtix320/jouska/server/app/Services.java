package com.gitlab.tixtix320.jouska.server.app;


import com.gitlab.tixtix320.jouska.server.service.GameService;
import com.gitlab.tixtix320.sonder.api.server.Sonder;

import java.io.IOException;
import java.util.Collections;

public class Services {
    public static GameService GAME_SERVICE;

    private static Sonder server;

    public static void initialize(int port) {
        if (server != null) {
            throw new IllegalStateException("Server already start, maybe you wish reconnect?");
        }
        String servicesPackage = "com.gitlab.tixtix320.jouska.server.service";
        server = Sonder.run(port, Collections.singletonList(servicesPackage), Collections.singletonList(servicesPackage));
        initServices();
    }

    public static void stop() throws IOException {
        if (server == null) {
            throw new IllegalStateException("Server does not started");
        }
        server.close();
    }

    private static void initServices() {
        GAME_SERVICE = server.getService(GameService.class);
    }
}

