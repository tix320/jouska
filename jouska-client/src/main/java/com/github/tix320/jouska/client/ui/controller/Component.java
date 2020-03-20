package com.github.tix320.jouska.client.ui.controller;

import javafx.scene.Parent;

public interface Component {

	Parent getRoot();

	Controller<?> getController();
}
