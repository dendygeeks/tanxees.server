package bfbc.tank.core;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class Missile extends Unit {
	private static final double SIZE = 10;
	
	private double velX, velY;
	
	private BoxConstructionCollider<Box> collider;
	private MissileCrashListener crashListener;

	Missile(MissileCrashListener crashListener, BoxConstructionCollider<Box> collider, double posX, double posY, double angle, double velocity)
	{
		super(SIZE, SIZE, posX, posY, angle);
		this.crashListener = crashListener;
		this.collider = collider;
		this.velX = velocity * Math.cos(angle * Math.PI / 180.0);
		this.velY = velocity * Math.sin(angle * Math.PI / 180.0);
	}
	
	public void frameStep() {
		DeltaXY delta = new DeltaXY(velX * Game.MODEL_TICK, velY * Game.MODEL_TICK);
		BoxConstructionCollider<Box>.MoveRotateResult mr = collider.tryMove(this, delta);
		
		if (!mr.targets.isEmpty()) {
			crashListener.missileCrashed(this, mr.mostAggressiveIntersectionTarget());
		}
	}
}
