package com.github.tix320.jouska.client.ui.game;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.tix320.jouska.client.ui.helper.transtion.Transitions;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.skimp.api.check.Try;
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

	private Supplier<Color> hoverColorFactory = () -> Color.rgb(181, 167, 167);

	public Tile() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/game/tile.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);
		setBorder(new Border(new BorderStroke(DEFAULT_BORDER_COLOR, BorderStrokeStyle.SOLID, new CornerRadii(5),
				new BorderWidths(2))));
		setOnMouseEntered(event -> {
			Color hoverColor = hoverColorFactory.get();
			if (hoverColor != null) {
				KeyFrame[] keyFrames = createBackgroundAppearKeyFrames(hoverColor, 0, 200);

				Timeline timeline = new Timeline(keyFrames);
				timeline.play();
			}

		});
		setOnMouseExited(event -> {
			Color hoverColor = hoverColorFactory.get();
			if (hoverColor != null) {
				KeyFrame[] keyFrames = createBackgroundDisAppearKeyFrames(hoverColorFactory.get(), 0, 200);

				Timeline timeline = new Timeline(keyFrames);
				timeline.play();
			}
		});
	}

	public void setHoverColorFactory(Supplier<Color> hoverColorFactory) {
		this.hoverColorFactory = hoverColorFactory;
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
		KeyFrame[] highFrames = createBackgroundAppearKeyFrames(color, 0, 200);
		Duration time = highFrames[highFrames.length - 1].getTime();
		KeyFrame[] lowFrames = createBackgroundDisAppearKeyFrames(color, (int) time.toMillis(), 200);

		Timeline timeline = new Timeline();
		timeline.getKeyFrames().addAll(highFrames);
		timeline.getKeyFrames().addAll(lowFrames);
		timeline.setOnFinished(event -> setBackground(null));
		return timeline;
	}

	private KeyFrame[] createBackgroundAppearKeyFrames(Color color, int millisStart, int durationMillis) {
		int chunks = durationMillis / 10;
		double chunkPercentage = 1.0 / chunks;

		int[] mills = {millisStart - 10};

		return Stream.iterate(0.0, i -> i + chunkPercentage)
				.limit(chunks)
				.map(i -> new Color(color.getRed(), color.getGreen(), color.getBlue(), i))
				.map(this::createBackground)
				.map(border -> new KeyFrame(Duration.millis(mills[0] += 10),
						new KeyValue(backgroundProperty(), border)))
				.toArray(KeyFrame[]::new);
	}

	private KeyFrame[] createBackgroundDisAppearKeyFrames(Color color, int millisStart, int durationMillis) {
		int chunks = durationMillis / 10;
		double chunkPercentage = 1.0 / chunks;

		int[] mills = {millisStart - 10};

		return Stream.iterate(1.0, i -> i - chunkPercentage)
				.limit(chunks)
				.map(i -> new Color(color.getRed(), color.getGreen(), color.getBlue(), i))
				.map(this::createBackground)
				.map(border -> new KeyFrame(Duration.millis(mills[0] += 10),
						new KeyValue(backgroundProperty(), border)))
				.toArray(KeyFrame[]::new);
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
