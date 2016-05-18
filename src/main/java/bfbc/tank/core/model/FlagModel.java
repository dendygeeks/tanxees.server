package bfbc.tank.core.model;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.Flag;

public class FlagModel extends UnitModel implements Flag {

	@Expose
	private boolean isCrashed = false;
	
	public FlagModel(double sizeX, double sizeY, double posX, double posY, double angle, boolean isCrashed) {
		super(sizeX, sizeY, posX, posY, angle);
		this.isCrashed = isCrashed;
	}

	public void setCrashed(boolean isCrashed) {
		this.isCrashed = isCrashed;
	}
	
	public boolean isCrashed() {
		return isCrashed;
	}

}
