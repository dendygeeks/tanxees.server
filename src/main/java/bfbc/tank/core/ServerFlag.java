package bfbc.tank.core;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.Flag;

public class ServerFlag extends ServerUnit implements Flag {
	private static final double SIZE = 44;
	
	@Expose
	private boolean isCrashed = false;
	
	ServerFlag(double posX, double posY)
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
