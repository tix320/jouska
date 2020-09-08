package com.github.tix320.jouska.client.ui.helper.component;

import com.github.tix320.jouska.client.ui.helper.FXHelper;
import com.github.tix320.skimp.api.check.Try;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;

/**
 * @author Tigran Sargsyan on 09-May-20.
 */
public class SpeedSlider extends AnchorPane {

	private final SimpleDoubleProperty gameSpeedCoefficient = new SimpleDoubleProperty(1);

	@FXML
	private Label label;

	@FXML
	private Slider slider;

	private String labelPrefix;

	public SpeedSlider() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/common/speed-slider.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);

		gameSpeedCoefficient.bindBidirectional(slider.valueProperty());
		gameSpeedCoefficient.addListener((observable, oldValue, newValue) -> this.label.setText(
				String.format("%s (X%.1f)", labelPrefix, newValue.doubleValue())));
	}

	public double getGameSpeedCoefficient() {
		return gameSpeedCoefficient.get();
	}

	public SimpleDoubleProperty speedCoefficientProperty() {
		return gameSpeedCoefficient;
	}

	public void setMinValue(double value) {
		slider.setMin(value);
	}

	public void setMaxValue(double value) {
		slider.setMax(value);
	}

	public void setLabel(String label) {
		FXHelper.checkFxThread();
		this.labelPrefix = label;
	}

	public void setValue(double value) {
		slider.setValue(value);
	}
}
