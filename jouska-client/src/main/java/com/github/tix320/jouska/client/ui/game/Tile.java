package com.github.tix320.jouska.client.ui.game;

import java.util.stream.Stream;

import com.github.tix320.jouska.client.ui.transtion.Transitions;
import com.github.tix320.jouska.core.model.Player;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.util.Duration;

public class Tile extends Region {


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

	private static final double ANIMATION_SECONDS = 0.4;

	private static final Color DEFAULT_BORDER_COLOR = Color.GRAY;

	private final ImageView content;

	public Tile() {
		this.content = new ImageView();
		getChildren().add(content);
		setBorder(createBorder(DEFAULT_BORDER_COLOR));
	}

	public Node content() {
		return content;
	}

	public Timeline animateBorder(Color color) {
		int[] mills = {-10};
		KeyFrame[] keyFrames = Stream.iterate(0.0, i -> i + 0.02)
				.limit(50)
				.map(i -> new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, DEFAULT_BORDER_COLOR),
						new Stop(i, color), new Stop(1, DEFAULT_BORDER_COLOR)))
				.map(this::createBorder)
				.map(border -> new KeyFrame(Duration.millis(mills[0] += 10), new KeyValue(borderProperty(), border)))
				.toArray(KeyFrame[]::new);

		Timeline timeline = new Timeline(keyFrames);
		timeline.setOnFinished(event -> setBorder(createBorder(DEFAULT_BORDER_COLOR)));
		return timeline;
	}

	public Transition disappearTransition() {
		return disappearTransition(Duration.seconds(ANIMATION_SECONDS));
	}

	public Transition appearTransition(Player player, int points) {
		Transition transition = appearTransition(Duration.seconds(ANIMATION_SECONDS));

		String imagePath = jouskas[player.ordinal()][points - 1];

		return Transitions.intercept(transition, () -> content.setImage(new Image(imagePath)));
	}

	public Transition disAppearAndAppearTransition(Player player, int points) {
		String imagePath = jouskas[player.ordinal()][points - 1];
		Transition disappearTransition = disappearTransition(Duration.seconds(ANIMATION_SECONDS / 2));
		Transition appearTransition = Transitions.intercept(appearTransition(Duration.seconds(ANIMATION_SECONDS / 2)),
				() -> content.setImage(new Image(imagePath)));

		return new SequentialTransition(disappearTransition, appearTransition);
	}

	private Transition appearTransition(Duration duration) {
		FadeTransition appearTransition = new FadeTransition(duration, content);
		appearTransition.setFromValue(0);
		appearTransition.setToValue(1);
		return appearTransition;
	}

	private Transition disappearTransition(Duration duration) {
		FadeTransition disappearTransition = new FadeTransition(duration, content);
		disappearTransition.setFromValue(1);
		disappearTransition.setToValue(0);
		return disappearTransition;
	}

	private Border createBorder(Paint paint) {
		return new Border(new BorderStroke(paint, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(2)));
	}
}
