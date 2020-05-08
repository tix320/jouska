package com.github.tix320.jouska.client.ui.game;

import java.util.stream.Stream;

import com.github.tix320.jouska.client.ui.helper.transtion.Transitions;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.kiwi.api.check.Try;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.util.Duration;

public class Tile extends VBox {

	private static final String[][] SHAPE_ICONS = new String[][]{
			{
					"images/game-shapes/blue-jouska-1.png", "images/game-shapes/blue-jouska-2.png",
					"images/game-shapes/blue-jouska-3.png", "images/game-shapes/blue-jouska-4.png"}, {
					"images/game-shapes/green-jouska-1.png", "images/game-shapes/green-jouska-2.png",
					"images/game-shapes/green-jouska-3.png", "images/game-shapes/green-jouska-4.png"}, {
					"images/game-shapes/red-jouska-1.png", "images/game-shapes/red-jouska-2.png",
					"images/game-shapes/red-jouska-3.png", "images/game-shapes/red-jouska-4.png"}, {
					"images/game-shapes/yellow-jouska-1.png", "images/game-shapes/yellow-jouska-2.png",
					"images/game-shapes/yellow-jouska-3.png", "images/game-shapes/yellow-jouska-4.png"},};

	private static final Color DEFAULT_BORDER_COLOR = Color.GRAY;

	@FXML
	private ImageView imageHolder;

	public Tile() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/game/tile.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);
		setBorder(new Border(new BorderStroke(DEFAULT_BORDER_COLOR, BorderStrokeStyle.SOLID, new CornerRadii(5),
				new BorderWidths(2))));
	}

	public Transition makeAppearTransition(PlayerColor player, int points, Duration duration) {
		Transition transition = makeAppearTransition(duration);

		Transition backgroundTransition = Transitions.timeLineToTransition(
				animateBackground(duration, Color.web(player.getColorCode())));

		String imagePath = SHAPE_ICONS[player.ordinal()][points - 1];

		return new ParallelTransition(backgroundTransition,
				Transitions.intercept(transition, () -> imageHolder.setImage(new Image(imagePath))));
	}

	public Transition makeDisAppearAndAppearTransition(PlayerColor player, int points, Duration baseDuration) {
		Transition backgroundTransition = Transitions.timeLineToTransition(
				animateBackground(baseDuration, Color.web(player.getColorCode())));

		String imagePath = SHAPE_ICONS[player.ordinal()][points - 1];
		Transition disappearTransition = makeDisappearTransition(baseDuration.divide(2));
		Transition appearTransition = Transitions.intercept(makeAppearTransition(baseDuration.divide(2)),
				() -> imageHolder.setImage(new Image(imagePath)));

		return new ParallelTransition(backgroundTransition,
				new SequentialTransition(disappearTransition, appearTransition));
	}

	public Transition makeDisappearTransition(Duration duration) {
		FadeTransition disappearTransition = new FadeTransition(duration, imageHolder);
		disappearTransition.setFromValue(1);
		disappearTransition.setToValue(0);
		return disappearTransition;
	}

	private Transition makeAppearTransition(Duration duration) {
		FadeTransition appearTransition = new FadeTransition(duration, imageHolder);
		appearTransition.setFromValue(0);
		appearTransition.setToValue(1);
		return appearTransition;
	}

	private Background createBackground(Paint paint) {
		return new Background(new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY));
	}

	public Timeline animateBackground(Color color) {
		int durationMillis = 400;

		int chunks = durationMillis / 10;
		double chunkPercentage = (double) 1 / chunks;
		int[] mills = {-10};
		KeyFrame[] highFrames = Stream.iterate(0.0, i -> i + chunkPercentage)
				.limit(chunks / 2)
				.map(i -> new Color(color.getRed(), color.getGreen(), color.getBlue(), i))
				.map(this::createBackground)
				.map(border -> new KeyFrame(Duration.millis(mills[0] += 10),
						new KeyValue(backgroundProperty(), border)))
				.toArray(KeyFrame[]::new);

		KeyFrame[] lowFrames = Stream.iterate(chunkPercentage * chunks / 2, i -> i - chunkPercentage)
				.limit(chunks / 2)
				.map(i -> new Color(color.getRed(), color.getGreen(), color.getBlue(), i))
				.map(this::createBackground)
				.map(border -> new KeyFrame(Duration.millis(mills[0] += 10),
						new KeyValue(backgroundProperty(), border)))
				.toArray(KeyFrame[]::new);

		Timeline timeline = new Timeline();
		timeline.getKeyFrames().addAll(highFrames);
		timeline.getKeyFrames().addAll(lowFrames);
		timeline.setOnFinished(event -> setBackground(null));
		return timeline;
	}

	private Timeline animateBackground(Duration duration, Color color) {
		int chunks = (int) duration.toMillis() / 10;
		double chunkPercentage = (double) 1 / chunks;
		int[] mills = {-10};
		KeyFrame[] keyFrames = Stream.iterate(0.0, i -> i + chunkPercentage)
				.limit(chunks)
				.map(i -> new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.TRANSPARENT),
						new Stop(i, color), new Stop(1, Color.TRANSPARENT)))
				.map(this::createBackground)
				.map(border -> new KeyFrame(Duration.millis(mills[0] += 10),
						new KeyValue(backgroundProperty(), border)))
				.toArray(KeyFrame[]::new);

		Timeline timeline = new Timeline(keyFrames);
		timeline.setOnFinished(event -> setBackground(null));
		return timeline;
	}
}
