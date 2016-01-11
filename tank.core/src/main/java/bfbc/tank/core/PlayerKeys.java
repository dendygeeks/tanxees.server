package bfbc.tank.core;

import com.google.gson.annotations.Expose;

class PlayerKeys {
	@Expose
	private boolean right;
	@Expose
	private boolean up;
	@Expose
	private boolean left;
	@Expose
	private boolean down;
	@Expose
	private boolean fire;

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
	public boolean isFire() {
		return fire;
	}

	public static PlayerKeys fromJson(String json) {
		return GlobalServices.getGson().fromJson(json, PlayerKeys.class);
	}
}
