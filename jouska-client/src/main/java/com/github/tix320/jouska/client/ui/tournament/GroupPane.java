package com.github.tix320.jouska.client.ui.tournament;

import java.util.Iterator;
import java.util.Set;

import com.github.tix320.jouska.core.dto.TournamentStructure.GroupView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.check.Try;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public final class GroupPane extends AnchorPane {

	private final GroupView groupView;

	public GroupPane(GroupView groupView) {
		this.groupView = groupView;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/tournament/group-pane.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.supplyOrRethrow(fxmlLoader::load);
		initView();
	}

	private void initView() {
		@SuppressWarnings("unchecked")
		Set<Label> memberLabels = (Set) lookupAll(".member");
		Set<Player> players = groupView.getPlayers();
		Iterator<Player> iterator = players.iterator();
		for (Label memberLabel : memberLabels) {
			if (iterator.hasNext()) {
				Player player = iterator.next();
				memberLabel.setText(player.getNickname());
			}
			else {
				memberLabel.setText("--");
			}
		}
	}
}
