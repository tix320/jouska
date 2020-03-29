package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ErrorController implements Controller<String> {

	@FXML
	TextArea errorTextArea;

	@Override
	public void init(String error) {
		errorTextArea.setText(error);
	}

	@Override
	public void destroy() {

	}

	public void returnToGame(ActionEvent event) {
		UI.switchComponent(ComponentType.MENU);
	}
}
