package bfbc.tank.core;

import com.google.gson.annotations.Expose;

class PlayerCommand {
	@Expose
	private boolean right;
	@Expose
	private boolean up;
	@Expose
	private boolean left;
	@Expose
	private boolean down;

	public boolean isRight() {
		return right;
	}
	public boolean isUp() {
		return up;
	}
	public boolean isLeft() {
		return left;
	}
	public boolean isDown() {
		return down;
	}

	public PlayerCommand(boolean right, boolean up, boolean left, boolean down) {
		this.right = right;
		this.up = up;
		this.left = left;
		this.down = down;
	}
	public static PlayerCommand fromJson(String json) {
		return GlobalServices.getGson().fromJson(json, PlayerCommand.class);
	}
}
