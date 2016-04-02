package bfbc.tank.core;

import java.util.HashMap;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class PlayerUnit extends Unit {
	
	private static final double SIZE = 34;
	
	private static final HashMap<Direction, Double> DIRECTION_ANGLES;
	static {
		DIRECTION_ANGLES = new HashMap<>();
		DIRECTION_ANGLES.put(Direction.RIGHT, 0d);
		DIRECTION_ANGLES.put(Direction.DOWN, 90d);
		DIRECTION_ANGLES.put(Direction.LEFT, 180d);
		DIRECTION_ANGLES.put(Direction.UP, 270d);
	}
	
	@Expose
	private boolean moving;
	
	private boolean wantToFire;
	
	private double velocity = 85.0d;
	private double angleVel = 500;

	private double dragVelocity = 3*velocity;	// From the ceiling

	private BoxConstructionCollider<Box> collider;
	private MissileCrashListener crashListener;
	
	public boolean isMoving() {
		return moving;
	}

	private PlayerKeys activeCommand;
	
	private Direction direction;

	public Direction getDirection() {
		return direction;
	}
	
	public PlayerKeys getActiveCommand() {
		return activeCommand;
	}
	
	public void setActiveCommand(PlayerKeys activeCommand) {
		this.activeCommand = activeCommand;
	}
	
	public PlayerUnit(Game game, BoxConstructionCollider<Box> collider, MissileCrashListener crashListener, Direction direction, PlayerKeys activeCommand, boolean moving, double posX, double posY, Double angle) {
		super(game, SIZE, SIZE, posX, posY, angle != null ? angle : DIRECTION_ANGLES.get(direction));
		this.collider = collider;
		this.crashListener = crashListener;
		this.activeCommand = activeCommand;
		this.direction = direction;
		this.moving = moving;
	}

	public PlayerUnit(Game game, BoxConstructionCollider<Box> collider, MissileCrashListener crashListener, Direction direction, PlayerKeys activeCommand, boolean moving, double posX, double posY) {
		this(game, collider, crashListener, direction, activeCommand, moving, posX, posY, null);
	}

	private void safeMove(DeltaXY dxy) {
		BoxConstructionCollider<Box>.MoveRotateResult mr = collider.tryMove(this, dxy);
		for (BoxConstruction<Box> t : mr.targets.keySet()) {
			if (t instanceof Missile) {
				crashListener.missileCrashed((Missile)t, this);
			}
		}
	}
	
	public void frameStep() {
		moving = false;
		if (activeCommand.isDown()) {
			direction = Direction.DOWN;
			moving = true;
		} else if (activeCommand.isLeft()) {
			direction = Direction.LEFT;
			moving = true;
		} else if (activeCommand.isRight()) {
			direction = Direction.RIGHT;
			moving = true;
		} else if (activeCommand.isUp()) {
			direction = Direction.UP;
			moving = true;
		}
		
		if (activeCommand.isFire()) {
			wantToFire = true;
		}

		double angleDelta = DIRECTION_ANGLES.get(direction) - getAngle();
		if (angleDelta < -180) angleDelta += 360;
		if (angleDelta > 180) angleDelta -= 360;
		
		boolean notRotating = false;
		double angleSmall = angleVel / 50;
		if (angleDelta > angleSmall) {
			angle += angleVel * Game.MODEL_TICK;
			moving = true;
		} else if (angleDelta < -angleSmall) {
			angle -= angleVel * Game.MODEL_TICK;
			moving = true;
		} else {
			angle = DIRECTION_ANGLES.get(direction);
			notRotating = true;
		}
		
		// Dragging. A small hack that makes driving around corners easier for the user 
		if (!notRotating) {
			double cs = getGame().cellSize;
			double dx = ((posX - cs/2) % cs) / cs;
			double dy = ((posY - cs/2) % cs) / cs;
			if (dx > 0.5d) dx -= 1.0d;
			if (dy > 0.5d) dy -= 1.0d;
			
			// We should drag the unit along the normal direction
			double dirAng = DIRECTION_ANGLES.get(direction) / 180.0d * Math.PI;
			double normX = -Math.sin(dirAng), 
			       normY = Math.cos(dirAng);
			double oldDirX = Math.cos(angle / 180.0d * Math.PI),
			       oldDirY = Math.sin(angle / 180.0d * Math.PI);
			
			double rotationFactor = Math.abs(oldDirX*normX + oldDirY*normY);
			double targetFactor = (-dx)*oldDirX + (-dy)*oldDirY;
			//if (targetFactor > 0) {	// We are only moving forward
				double velX = dragVelocity * targetFactor * oldDirX * rotationFactor,
				       velY = dragVelocity * targetFactor * oldDirY * rotationFactor;
	
				DeltaXY dxy = new DeltaXY(
						velX * Game.MODEL_TICK,
						velY * Game.MODEL_TICK
				);
				safeMove(dxy);
			//}
			
		}

		double displacement = velocity * Game.MODEL_TICK;
		
		if (notRotating && wantToFire) {
			double missileVelocity = velocity * 1.2; // Missile is a bit faster than tank
			if (moving) {
				missileVelocity += velocity;
			}
			getGame().createMissile(this, getPosX(), getPosY(), getAngle(), missileVelocity);
			wantToFire = false;
		}

		// If we are not rotating, we are moving
		if (notRotating && moving) {
			DeltaXY dxy = new DeltaXY(displacement * Math.cos(angle / 180 * Math.PI),
					displacement * Math.sin(angle / 180 * Math.PI));
			safeMove(dxy);
		}
	}
	
}