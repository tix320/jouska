package com.github.tix320.jouska.client.ui.game;

import java.util.stream.Stream;

import com.github.tix320.jouska.client.ui.transtion.Transitions;
import com.github.tix320.jouska.core.model.Player;
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

public class Tile extends AnchorPane {

	private static final String[][] jouskas = new String[][]{
			{
					"ui/game/blue-jouska-1.png",
					"ui/game/blue-jouska-2.png",
					"ui/game/blue-jouska-3.png",
					"ui/game/blue-jouska-4.png"
			},
			{
					"ui/game/green-jouska-1.png",
					"ui/game/green-jouska-2.png",
					"ui/game/green-jouska-3.png",
					"ui/game/green-jouska-4.png"
			},
			{
					"ui/game/red-jouska-1.png",
					"ui/game/red-jouska-2.png",
					"ui/game/red-jouska-3.png",
					"ui/game/red-jouska-4.png"
			},
			{
					"ui/game/yellow-jouska-1.png",
					"ui/game/yellow-jouska-2.png",
					"ui/game/yellow-jouska-3.png",
					"ui/game/yellow-jouska-4.png"
			},
	};

	public static final double PREF_SIZE = 110;

	private static final double ANIMATION_SECONDS = 0.3;

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

	public Transition disappearTransition() {
		return disappearTransition(Duration.seconds(ANIMATION_SECONDS));
	}

	public Transition appearTransition(Player player, int points) {
		Duration duration = Duration.seconds(ANIMATION_SECONDS);
		Transition transition = appearTransition(duration);

		Transition backgroundTransition = Transitions.timeLineToTransition(
				animateBackground(duration, Color.web(player.getColorCode())));

		String imagePath = jouskas[player.ordinal()][points - 1];

		return new ParallelTransition(backgroundTransition,
				Transitions.intercept(transition, () -> imageHolder.setImage(new Image(imagePath))));
	}

	public Transition disAppearAndAppearTransition(Player player, int points) {
		Transition backgroundTransition = Transitions.timeLineToTransition(
				animateBackground(Duration.seconds(ANIMATION_SECONDS), Color.web(player.getColorCode())));

		String imagePath = jouskas[player.ordinal()][points - 1];
		Transition disappearTransition = disappearTransition(Duration.seconds(ANIMATION_SECONDS / 2));
		Transition appearTransition = Transitions.intercept(appearTransition(Duration.seconds(ANIMATION_SECONDS / 2)),
				() -> imageHolder.setImage(new Image(imagePath)));

		return new ParallelTransition(backgroundTransition,
				new SequentialTransition(disappearTransition, appearTransition));
	}

	private Transition appearTransition(Duration duration) {
		FadeTransition appearTransition = new FadeTransition(duration, imageHolder);
		appearTransition.setFromValue(0);
		appearTransition.setToValue(1);
		return appearTransition;
	}

	private Transition disappearTransition(Duration duration) {
		FadeTransition disappearTransition = new FadeTransition(duration, imageHolder);
		disappearTransition.setFromValue(1);
		disappearTransition.setToValue(0);
		return disappearTransition;
	}

	private Background createBackground(Paint paint) {
		return new Background(new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY));
	}
}
