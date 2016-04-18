package bfbc.tank.core;

import com.google.gson.annotations.Expose;

public class Flag extends Unit {
	private static final double SIZE = 44;
	
	@Expose
	private boolean isCrashed = false;
	
	Flag(double posX, double posY)
	{
		super(SIZE, SIZE, posX, posY, 0);
	}
	
	public void setCrashed(boolean isCrashed) {
		this.isCrashed = isCrashed;
	}
	
	public boolean isCrashed() {
		return isCrashed;
	}
	
	@Override
	public boolean isActive() {
		return !isCrashed;
	}
}
