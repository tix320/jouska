package com.github.tix320.jouska.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ErrorController implements Controller<String> {

	@FXML
	TextArea errorTextArea;

	@Override
	public void initialize(String error) {
		errorTextArea.setText(error);
	}
}
