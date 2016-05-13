package bfbc.tank.core;

import java.util.Iterator;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.Unit;
import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.DeltaAngle;
import bfbc.tank.core.mechanics.DeltaXY;

/**
 * Something that can move and rotate
 */
public class ServerUnit implements Unit, Box, BoxConstruction<Box> {
	
	@Expose
	protected double sizeX, sizeY;
	@Expose
	protected double posX;
	@Expose
	protected double posY;
	@Expose
	protected double angle;
	
	public ServerUnit(double sizeX, double sizeY, double posX, double posY, double angle) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.posX = posX;
		this.posY = posY;
		this.angle = angle;
	}

	public double getPosX() {
		return posX;
	}
	public double getPosY() {
		return posY;
	}
	public double getAngle() {
		return angle;
	}
	
	@Override
	public double getExcentricity() {
		return Math.abs(sizeX - sizeY) / 2;
	}
	
	public String toJson() {
		return GlobalServices.getGson().toJson(this);
	}
	@Override
	public double getBottom() {
		return posY + sizeY / 2;
	}
	
	@Override
	public double getLeft() {
		return posX - sizeX / 2;
	}
	
	@Override
	public double getRight() {
		return posX + sizeX / 2;
	}
	
	@Override
	public double getTop() {
		return posY - sizeY / 2;
	}
	
	@Override
	public void move(DeltaXY delta) {
		posX += delta.x;
		posY += delta.y;
	}
	
	@Override
	public void rotate(DeltaAngle delta) {
		switch (delta) {
		case ZERO:
			break; 	
		case PI_BY_2:
			double t = sizeX;
			sizeX = sizeY;
			sizeY = t;
			break;
		case PI:
			break;
		case THREE_PI_BY_2:
			t = sizeX;
			sizeX = sizeY;
			sizeY = t;
			break;
		}
	}
	
	@Override
	public boolean isActive() {
		return true;
	}
	

	@Override
	public Iterator<Box> iterator() {
		Iterator<Box> res = new Iterator<Box>() {
			
			private boolean hasNext = true;
			
			@Override
			public Box next() {
				hasNext = false;
				return ServerUnit.this;
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
		return res;
	}

	@Override
	public double getSizeX() {
		return sizeX;
	}

	@Override
	public double getSizeY() {
		return sizeY;
	}
}
