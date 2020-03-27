package com.github.tix320.jouska.client.ui.helper.component;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class NumberTextField extends TextField {

	private final SimpleIntegerProperty number = new SimpleIntegerProperty();

	public NumberTextField() {
		TextFields.makeNumeric(this);

		Bindings.bindBidirectional(textProperty(), numberProperty(), new StringConverter<>() {
			@Override
			public String toString(Number object) {
				return object.toString();
			}

			@Override
			public Number fromString(String string) {
				return string.isEmpty() ? 0 : Integer.parseInt(string);
			}
		});
	}

	public int getNumber() {
		return number.get();
	}

	public SimpleIntegerProperty numberProperty() {
		return number;
	}

	public void setNumber(int number) {
		this.number.set(number);
	}
}
