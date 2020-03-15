package com.github.tix320.jouska.server.entity;

import java.util.Objects;

import com.github.tix320.jouska.core.model.RoleName;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

@Entity("players")
@Indexes(@Index(fields = {@Field("nickname")}, options = @IndexOptions(unique = true)))
public class PlayerEntity {

	@Id
	private ObjectId id;

	private String nickname;

	private String password;

	private RoleName role;

	public PlayerEntity() {
	}

	public PlayerEntity(String nickname, String password, RoleName role) {
		this.nickname = nickname;
		this.password = password;
		this.role = role;
	}

	public ObjectId getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPassword() {
		return password;
	}

	public RoleName getRole() {
		return role;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PlayerEntity that = (PlayerEntity) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
