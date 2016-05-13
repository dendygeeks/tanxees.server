package bfbc.tank.core;

import bfbc.tank.core.api.Missile;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class ServerMissile extends ServerUnit implements Missile {
	private static final double SIZE = 10;
	
	private double velX, velY;
	
	private BoxConstructionCollider collider;
	private MissileCrashListener crashListener;

	ServerMissile(MissileCrashListener crashListener, BoxConstructionCollider collider, double posX, double posY, double angle, double velocity)
	{
		super(SIZE, SIZE, posX, posY, angle);
		this.crashListener = crashListener;
		this.collider = collider;
		this.velX = velocity * Math.cos(angle * Math.PI / 180.0);
		this.velY = velocity * Math.sin(angle * Math.PI / 180.0);
	}
	
	public void frameStep() {
		DeltaXY delta = new DeltaXY(velX * ServerGame.MODEL_TICK, velY * ServerGame.MODEL_TICK);
		BoxConstructionCollider.MoveRotateResult mr = collider.tryMove(this, delta);
		
		if (!mr.targets.isEmpty()) {
			crashListener.missileCrashed(this, /*mr.mostAggressiveIntersectionTarget()*/ mr.targets.keySet() );
		}
	}
}
