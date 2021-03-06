package com.github.tix320.jouska.client.ui.tournament;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.tix320.jouska.client.ui.helper.FXHelper;
import com.github.tix320.jouska.core.dto.TournamentStructure.GroupPlayerView;
import com.github.tix320.jouska.core.dto.TournamentStructure.GroupView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public final class GroupPane extends AnchorPane {

	@FXML
	private Label groupNameLabel;

	private final GroupView groupView;

	public GroupPane(GroupView groupView) {
		FXHelper.loadFxmlForController("/ui/tournament/group-pane.fxml", this);
		this.groupView = groupView;
		initView();
	}

	private void initView() {
		groupNameLabel.setText(groupView.getGroupName());
		@SuppressWarnings("all")
		Set<HBox> memberLabels = (Set) lookupAll(".member");
		List<GroupPlayerView> players = groupView.getPlayerViews();
		Iterator<GroupPlayerView> iterator = players.iterator();
		for (HBox member : memberLabels) {
			Label nameLabel = (Label) member.lookup(".nameLabel");
			Label groupPointsLabel = (Label) member.lookup(".groupPointsLabel");
			Label gamesPointsLabel = (Label) member.lookup(".gamesPointsLabel");
			if (iterator.hasNext()) {
				GroupPlayerView playerView = iterator.next();
				nameLabel.setText(playerView.getPlayer().getNickname());
				groupPointsLabel.setText(String.valueOf(playerView.getGroupPoints()));
				gamesPointsLabel.setText(String.valueOf(playerView.getGamesPoints()));
			} else {
				nameLabel.setText("--");
				groupPointsLabel.setText("-");
				gamesPointsLabel.setText("-");
			}
		}
	}
}
