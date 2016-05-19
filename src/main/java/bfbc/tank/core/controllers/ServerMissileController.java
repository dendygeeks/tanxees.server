package bfbc.tank.core.controllers;

import bfbc.tank.api.model.MissileModel;
import bfbc.tank.core.MissileCrashListener;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class ServerMissileController extends ServerUnitController {
	private static final double SIZE = 10;
	
	private double velX, velY;
	
	private BoxConstructionCollider collider;
	private MissileCrashListener crashListener;

	public MissileModel getMissileModel() {
		return (MissileModel)getUnitModel();
	}
	
	ServerMissileController(MissileCrashListener crashListener, BoxConstructionCollider collider, double posX, double posY, double angle, double velocity)
	{
		super(new MissileModel(SIZE, SIZE, posX, posY, angle, false));
		this.crashListener = crashListener;
		this.collider = collider;
		this.velX = velocity * Math.cos(angle * Math.PI / 180.0);
		this.velY = velocity * Math.sin(angle * Math.PI / 180.0);
	}
	
	public void frameStep() {
		DeltaXY delta = new DeltaXY(velX * ServerGameController.MODEL_TICK, velY * ServerGameController.MODEL_TICK);
		BoxConstructionCollider.MoveRotateResult mr = collider.tryMove(this, delta);
		
		if (!mr.targets.isEmpty()) {
			crashListener.missileCrashed(this, /*mr.mostAggressiveIntersectionTarget()*/ mr.targets.keySet() );
		}
	}
}
