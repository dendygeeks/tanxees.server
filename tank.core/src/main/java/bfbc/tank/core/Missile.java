package bfbc.tank.core;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class Missile extends Unit {
	private static final double SIZE = 10;
	
	private double velX, velY;
	
	private BoxConstructionCollider<Box> collider;
	
	@Expose
	private int ownerPlayerId;
	
	private MissileCrashListener crashListener;

	Missile(Game game, MissileCrashListener crashListener, BoxConstructionCollider<Box> collider, int ownerPlayerId, double posX, double posY, double angle, double velocity)
	{
		super(game, SIZE, SIZE, posX, posY, angle);
		this.crashListener = crashListener;
		this.collider = collider;
		this.velX = velocity * Math.cos(angle * Math.PI / 180.0);
		this.velY = velocity * Math.sin(angle * Math.PI / 180.0);
		this.ownerPlayerId = ownerPlayerId;
	}
	
	public void frameStep() {
		DeltaXY delta = new DeltaXY(velX * Game.MODEL_TICK, velY * Game.MODEL_TICK);
		BoxConstructionCollider<Box>.MoveResult mr = collider.tryMove(this, delta);
		
		if (!mr.targets.isEmpty()) {
			crashListener.missileCrashed(this, mr.mostAggressiveIntersectionTarget());
		}
	}
	
	public int getOwnerPlayerId() {
		return ownerPlayerId;
	}
	
}
