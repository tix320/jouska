module jouska.client {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;

    requires sonder;
    requires kiwi;

    requires jouska.core;

    opens com.gitlab.tixtix320.jouska.client.app to javafx.base;
}