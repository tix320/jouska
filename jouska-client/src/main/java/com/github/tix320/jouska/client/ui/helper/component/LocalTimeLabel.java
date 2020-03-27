package com.github.tix320.jouska.client.ui.helper.component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class LocalTimeLabel extends Label {

	private final SimpleObjectProperty<LocalTime> time = new SimpleObjectProperty<>();

	private final SimpleObjectProperty<DateTimeFormatter> formatter = new SimpleObjectProperty<>(
			DateTimeFormatter.ISO_LOCAL_TIME);

	public LocalTimeLabel() {
		Bindings.bindBidirectional(textProperty(), timeProperty(), new StringConverter<>() {
			@Override
			public String toString(LocalTime object) {
				return (object == null ? LocalTime.MIN : object).format(formatter.get());
			}

			@Override
			public LocalTime fromString(String string) {
				return string == null ? LocalTime.MIN : LocalTime.parse(string, formatter.get());
			}
		});
	}

	public LocalTime getTime() {
		return time.get();
	}

	public SimpleObjectProperty<LocalTime> timeProperty() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time.set(time);
	}

	public DateTimeFormatter getFormatter() {
		return formatter.get();
	}

	public SimpleObjectProperty<DateTimeFormatter> formatterProperty() {
		return formatter;
	}

	public void setFormatter(DateTimeFormatter formatter) {
		this.formatter.set(formatter);
	}
}
