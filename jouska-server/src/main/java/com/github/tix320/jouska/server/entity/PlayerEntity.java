package com.github.tix320.jouska.server.entity;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity("players")
public class PlayerEntity {
	@Id
	private ObjectId id;

	private String fullName;

	private String nickname;

	private String password;

	private PlayerEntity() {
	}

	public PlayerEntity(String fullName, String nickname, String password) {
		this.fullName = fullName;
		this.nickname = nickname;
		this.password = password;
	}

	public ObjectId getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPassword() {
		return password;
	}
}
