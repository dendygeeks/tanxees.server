package bfbc.tank.core;

import java.util.Iterator;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class Missile implements Box, BoxConstruction<Box> {
	private static final double SIZE = 10;
	
	@Expose
	private double posX;
	@Expose
	private double posY;
	@Expose
	private double angle;

	private double velX, velY;
	
	private BoxConstructionCollider<Box> collider;
	
	private Player ownerPlayer;
	
	private MissileCrashListener crashListener;

	Missile(MissileCrashListener crashListener, BoxConstructionCollider<Box> collider, Player ownerPlayer, double posX, double posY, double angle, double velocity)
	{
		this.crashListener = crashListener;
		this.collider = collider;
		this.posX = posX;
		this.posY = posY;
		this.angle = angle;
		this.velX = velocity * Math.cos(angle * Math.PI / 180.0);
		this.velY = velocity * Math.sin(angle * Math.PI / 180.0);
		this.ownerPlayer = ownerPlayer;
	}
	
	public void frameStep() {
		DeltaXY delta = new DeltaXY(velX * Game.MODEL_TICK, velY * Game.MODEL_TICK);
		BoxConstructionCollider<Box>.MoveResult mr = collider.tryMove(Missile.this, delta);
		
		if (mr.targets.length > 0) {
			crashListener.missileCrashed(this, mr.targets[0]);	// TODO Choose which one to destroy here
		}
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
	
	public Player getOwnerPlayer() {
		return ownerPlayer;
	}
	
	public String toJson() {
		return GlobalServices.getGson().toJson(this);
	}
	
	@Override
	public Iterator<Box> iterator() {
		Iterator<Box> res = new Iterator<Box>() {
			
			private boolean hasNext = true;
			
			@Override
			public Box next() {
				hasNext = false;
				return Missile.this;
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
		return res;
	}

	@Override
	public double getBottom() {
		return posY + SIZE;
	}
	
	@Override
	public double getLeft() {
		return posX;
	}
	
	@Override
	public double getRight() {
		return posX + SIZE;
	}
	
	@Override
	public double getTop() {
		return posY;
	}
	
	@Override
	public void move(DeltaXY delta) {
		posX += delta.x;
		posY += delta.y;
	}
	@Override
	public boolean isActive() {
		return true;
	}
	
}
