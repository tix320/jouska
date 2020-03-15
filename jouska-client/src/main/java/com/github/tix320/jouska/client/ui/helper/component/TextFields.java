package com.github.tix320.jouska.client.ui.helper.component;

import javafx.scene.control.TextField;

public class TextFields {

	public static void makeNumeric(TextField textField) {
		textField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				textField.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
	}
}
