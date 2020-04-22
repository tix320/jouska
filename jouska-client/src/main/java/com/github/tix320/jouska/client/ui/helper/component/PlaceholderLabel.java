package com.github.tix320.jouska.client.ui.helper.component;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class PlaceholderLabel extends Label {

	SimpleStringProperty template = new SimpleStringProperty();

	public PlaceholderLabel() {
		textProperty().addListener((observable, oldValue, newValue) -> {

		});
		Bindings.bindBidirectional(textProperty(), templateProperty(), new MessageFormat(template.get()));
	}

	public String getTemplate() {
		return template.get();
	}

	public SimpleStringProperty templateProperty() {
		return template;
	}

	public void setTemplate(String template) {
		this.template.set(template);
	}
}
