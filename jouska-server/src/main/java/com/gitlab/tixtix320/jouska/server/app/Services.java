package com.gitlab.tixtix320.jouska.server.app;


import com.gitlab.tixtix320.sonder.api.server.Sonder;

import java.io.IOException;

public class Services {
    public static Sonder SONDER;

    public static void initialize(int port) {
        if (SONDER != null) {
            throw new IllegalStateException("Server already start, maybe you wish reconnect?");
        }
        String servicesPackage = "com.gitlab.tixtix320.jouska.server.service";
        SONDER = Sonder.withBuiltInProtocols(port, servicesPackage);
        initServices();
    }

    public static void stop() throws IOException {
        if (SONDER == null) {
            throw new IllegalStateException("Server does not started");
        }
        SONDER.close();
    }

    private static void initServices() {

    }
}

