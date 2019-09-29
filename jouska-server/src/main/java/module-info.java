 module jouska.server {
    requires sonder;
    requires kiwi;
    requires jouska.core;

    opens com.gitlab.tixtix320.jouska.server.service to sonder,kiwi,java.base;
    exports com.gitlab.tixtix320.jouska.server.service to sonder;
}