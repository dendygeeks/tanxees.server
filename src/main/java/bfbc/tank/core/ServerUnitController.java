package bfbc.tank.core;

import java.util.Iterator;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.DeltaAngle;
import bfbc.tank.core.mechanics.DeltaXY;
import bfbc.tank.core.model.UnitModel;

/**
 * Something that can move and rotate
 */
public class ServerUnitController implements Box, BoxConstruction<Box> {
	
	private UnitModel unitModel;
	
	public ServerUnitController() {
		
	}

	public ServerUnitController(UnitModel unitModel) {
		this.unitModel = unitModel;
	}
	
	public UnitModel getUnitModel() {
		return unitModel;
	}
	
	@Override
	public double getExcentricity() {
		return Math.abs(unitModel.getSizeX() - unitModel.getSizeY()) / 2;
	}
	
	public String toJson() {
		return GlobalServices.getGson().toJson(this);
	}
	@Override
	public double getBottom() {
		return unitModel.getPosY() + unitModel.getSizeY() / 2;
	}
	
	@Override
	public double getLeft() {
		return unitModel.getPosX() - unitModel.getSizeX() / 2;
	}
	
	@Override
	public double getRight() {
		return unitModel.getPosX() + unitModel.getSizeX() / 2;
	}
	
	@Override
	public double getTop() {
		return unitModel.getPosY() - unitModel.getSizeY() / 2;
	}
	
	@Override
	public void move(DeltaXY delta) {
		unitModel.setPosX(unitModel.getPosX() + delta.x);
		unitModel.setPosY(unitModel.getPosY() + delta.y);
	}
	
	@Override
	public void rotate(DeltaAngle delta) {
		switch (delta) {
		case ZERO:
			break; 	
		case PI_BY_2:
		case THREE_PI_BY_2:
			double t = unitModel.getSizeX();
			unitModel.setSizeX(unitModel.getSizeY());
			unitModel.setSizeY(t);
			break;
		case PI:
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
				return ServerUnitController.this;
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
		return res;
	}
	
	
}
