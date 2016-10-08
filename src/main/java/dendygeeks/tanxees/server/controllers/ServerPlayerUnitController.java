package dendygeeks.tanxees.server.controllers;

import java.util.Arrays;
import java.util.HashMap;

import dendygeeks.tanxees.api.java.interfaces.CellType;
import dendygeeks.tanxees.api.java.interfaces.DeltaAngle;
import dendygeeks.tanxees.api.java.interfaces.Direction;
import dendygeeks.tanxees.api.java.model.PlayerKeysModel;
import dendygeeks.tanxees.api.java.model.PlayerUnitModel;
import dendygeeks.tanxees.server.MissileCrashListener;
import dendygeeks.tanxees.server.SpawnConfig;
import dendygeeks.tanxees.server.mechanics.Box;
import dendygeeks.tanxees.server.mechanics.BoxConstruction;
import dendygeeks.tanxees.server.mechanics.BoxConstructionCollider;
import dendygeeks.tanxees.server.mechanics.DeltaXY;
import dendygeeks.tanxees.server.mechanics.BoxConstructionCollider.CollisionResult;
import dendygeeks.tanxees.server.mechanics.BoxConstructionCollider.MoveRotateResult;

public class ServerPlayerUnitController extends ServerUnitController {

	private static final double gravity = 9.81;
	private static final double SPAWN_INVINCIBILITY_DURATION = 3.0; // seconds
	//private static final double SIZE = 34;
	
	private static final HashMap<Direction, Double> DIRECTION_ANGLES;
	static {
		DIRECTION_ANGLES = new HashMap<>();
		DIRECTION_ANGLES.put(Direction.RIGHT, 0d);
		DIRECTION_ANGLES.put(Direction.DOWN, 90d);
		DIRECTION_ANGLES.put(Direction.LEFT, 180d);
		DIRECTION_ANGLES.put(Direction.UP, 270d);
	}
	
	private double cellSize;

	private boolean wantToFire;
	
	private double missileVelocity = 85.0 * 1.2;
	private double velocityX, velocityY;
	private double oldX, oldY;
	private double angleVel = 500;

	private double dragVelocity = 85.0d;	// From the ceiling

	private BoxConstructionCollider collider;
	private MissileCrashListener crashListener;
	
	private PlayerKeysModel activeCommand;
	
	private Direction direction;

	private ServerPlayerController player;

	public Direction getDirection() {
		return direction;
	}
	
	public PlayerKeysModel getActiveCommand() {
		return activeCommand;
	}
	
	public void setActiveCommand(PlayerKeysModel activeCommand) {
		this.activeCommand = activeCommand;
	}
	
	private static double sizeForDir(boolean isY /* if not, it is X */, double sizeW, double sizeL, Direction direction) {
		boolean dirV = (direction == Direction.UP || direction == Direction.DOWN);
		return (dirV ^ isY) ? sizeW : sizeL;
	}
	
	private SpawnConfig spawnConfig;
	private double sizeW, sizeL;
	private double mass, midship, forwardPower, backwardPower;
	
	public void respawnUnit() {
		getPlayerUnitModel().setSizeX(sizeForDir(false, sizeW, sizeL, spawnConfig.direction));
		getPlayerUnitModel().setSizeY(sizeForDir(true, sizeW, sizeL, spawnConfig.direction));
		getPlayerUnitModel().setPosX(spawnConfig.getPosX(cellSize));
		getPlayerUnitModel().setPosY(spawnConfig.getPosY(cellSize));
		getPlayerUnitModel().setAngle(DIRECTION_ANGLES.get(spawnConfig.direction));
		getPlayerUnitModel().setSpawnMoment((double)System.currentTimeMillis() / 1000);
		this.direction = spawnConfig.direction;
		this.velocityX = 0.0;
		this.velocityY = 0.0;
		this.oldX = getPlayerUnitModel().getPosX();
		this.oldY = getPlayerUnitModel().getPosY();
	}
	
	public ServerPlayerUnitController(ServerPlayerController player, double sizeW, double sizeL,
					double mass, double midship, double forwardPower, double backwardPower, double cellSize, 
					BoxConstructionCollider collider, MissileCrashListener crashListener, SpawnConfig spawnConfig, 
					double spawnMoment, PlayerKeysModel activeCommand, boolean moving, Double angle) {

		super(new PlayerUnitModel(sizeForDir(false, sizeW, sizeL, spawnConfig.direction), 
		      sizeForDir(true, sizeW, sizeL, spawnConfig.direction), 
		      spawnConfig.getPosX(cellSize), 
		      spawnConfig.getPosY(cellSize), 
		      angle != null ? angle : DIRECTION_ANGLES.get(spawnConfig.direction), spawnMoment, false));
		this.player = player;
		this.cellSize = cellSize;
		this.collider = collider;
		this.crashListener = crashListener;
		this.activeCommand = activeCommand;
		this.direction = spawnConfig.direction;
		this.spawnConfig = spawnConfig;
		this.sizeW = sizeW;
		this.sizeL = sizeL;
		this.mass = mass;
		this.midship = midship;
		this.forwardPower = forwardPower;
		this.backwardPower = backwardPower;
		this.velocityX = 0.0;
		this.velocityY = 0.0;
		this.oldX = getPlayerUnitModel().getPosX();
		this.oldY = getPlayerUnitModel().getPosY();
	}

	public PlayerUnitModel getPlayerUnitModel() {
		return (PlayerUnitModel)getUnitModel();
	}
	
	public ServerPlayerUnitController(
			ServerPlayerController player, 
			double sizeW, double sizeL, double mass, double midship, double forwardPower, double backwardPower,
			double cellSize, 
			BoxConstructionCollider collider, 
			MissileCrashListener crashListener, 
			SpawnConfig spawnConfig,
			double spawnMoment,
			PlayerKeysModel activeCommand, 
			boolean moving) {
		this(player, sizeW, sizeL, mass, midship, forwardPower, backwardPower, cellSize, collider, crashListener, spawnConfig, spawnMoment, activeCommand, moving, null);
	}

	private boolean safeMove(DeltaXY dxy) {
		DeltaXY dx = new DeltaXY(dxy.x, 0);
		DeltaXY dy = new DeltaXY(0, dxy.y);
		
		BoxConstructionCollider.MoveRotateResult mrx = collider.tryMove(this, dx);
		for (BoxConstruction<?> t : mrx.targets.keySet()) {
			if (t instanceof ServerMissileController) {
				crashListener.missileCrashed((ServerMissileController)t, Arrays.asList(new BoxConstruction[] { this }));
			}
		}
		BoxConstructionCollider.MoveRotateResult mry = collider.tryMove(this, dy);
		for (BoxConstruction<?> t : mry.targets.keySet()) {
			if (t instanceof ServerMissileController) {
				crashListener.missileCrashed((ServerMissileController)t, Arrays.asList(new BoxConstruction[] { this }));
			}
		}
		return mrx.targets.isEmpty() && mry.targets.isEmpty();
	}
	
	/*
	 * A strange physics occuring in this world:
	 		m * a = F_eng - F_dry - F_air
			F_dry = mu * m * g
			F_air = nu * S * v
			F_eng = F0 * mu 				// completely unphysical, but might be better to look at
		
		Parameters:
			tank-specific: m - mass, S - midship, F0 - effective engine power
			world-fixed: g - gravity
			cell-specific: mu - dry friction coefficient, nu - air friction coefficient
		Outcome:
			v - speed, a - acceleration
		
		For now use Euler integration:
			v1 = v0 + a * dt
			x1 = x0 + v0 * dt
	 */
	
	public void frameStep() {
		boolean moving = false;
		double angle = getPlayerUnitModel().getAngle();
		
		boolean moveBackwards = false;
		Direction newDir = direction;
		if (activeCommand.isDown()) {
			newDir = Direction.DOWN;
			moving = true;
		} else if (activeCommand.isLeft()) {
			newDir = Direction.LEFT;
			moving = true;
		} else if (activeCommand.isRight()) {
			newDir = Direction.RIGHT;
			moving = true;
		} else if (activeCommand.isUp()) {
			newDir = Direction.UP;
			moving = true;
		}
		
		DeltaAngle modelAngleDelta = Direction.sub(newDir, direction);
		if (modelAngleDelta != DeltaAngle.ZERO) {
			MoveRotateResult mrr = collider.tryRotate(this, modelAngleDelta);
			if (mrr.success) {
				direction = newDir;				
			} else {
				if (modelAngleDelta.angle == 180) {
					// We can't turn around cause there is no space to rotate.
					// So we will drive backwards
					moveBackwards = true;
				} else {
					moving = false;
				}
			}
		}
		
		if (activeCommand.isFire()) {
			wantToFire = true;
		}

		double visualAngleDelta = DIRECTION_ANGLES.get(direction) - angle;
		if (visualAngleDelta < -180) visualAngleDelta += 360;
		if (visualAngleDelta > 180) visualAngleDelta -= 360;
		
		boolean notRotating = false;
		double angleSmall = angleVel / 50;
		if (visualAngleDelta > angleSmall) {
			angle += angleVel * ServerGameController.MODEL_TICK;
			moving = true;
		} else if (visualAngleDelta < -angleSmall) {
			angle -= angleVel * ServerGameController.MODEL_TICK;
			moving = true;
		} else {
			angle = DIRECTION_ANGLES.get(direction);
			notRotating = true;
		}
		
		// Dragging. A small hack that makes driving around corners easier for the user
		if (!notRotating) {
			double cs = cellSize;
			double dx = ((getPlayerUnitModel().getPosX() - cs/2) % cs) / cs;
			double dy = ((getPlayerUnitModel().getPosY() - cs/2) % cs) / cs;
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

			double velX = dragVelocity * targetFactor * oldDirX * rotationFactor,
			       velY = dragVelocity * targetFactor * oldDirY * rotationFactor;

			DeltaXY dxy = new DeltaXY(
					velX * ServerGameController.MODEL_TICK,
					velY * ServerGameController.MODEL_TICK
			);
			
			safeMove(dxy);
		}

		double displacementX, displacementY;
		double oldVelocityX = velocityX, oldVelocityY = velocityY;
		{
			double directionAngle = DIRECTION_ANGLES.get(direction) / 180.0d * Math.PI;
			int cellX = (int) ((getPlayerUnitModel().getPosX() - cellSize / 2) / cellSize);
			int cellY = (int) ((getPlayerUnitModel().getPosY() - cellSize / 2) / cellSize);
			CellType cellUnderMe = player.getFieldCellType(cellX, cellY);
			if (notRotating && (moving || moveBackwards)) {
				// engine is on
				double engineForce = (!moveBackwards ? forwardPower : -backwardPower) * cellUnderMe.dryFriction;
				double dv = engineForce * ServerGameController.MODEL_TICK / mass;
				velocityX += Math.cos(directionAngle) * dv;
				velocityY += Math.sin(directionAngle) * dv;
			}

			// Calculating the friction
			double frictionForce = cellUnderMe.airFriction * midship * Math.hypot(velocityX, velocityY)
					+ cellUnderMe.dryFriction * mass * gravity;
			if (!notRotating) {
				// Additional friction on rotation
				frictionForce *= 2.0;
			}
			
			// Calculating velocity modification cause of friction
			double dv = frictionForce * ServerGameController.MODEL_TICK / mass;
			if (dv * Math.abs(Math.cos(directionAngle)) >= Math.abs(velocityX)) {
				velocityX = 0.0d;
			} else {
				velocityX -= dv * velocityX / Math.hypot(velocityX, velocityY);
			}
			if (dv * Math.abs(Math.sin(directionAngle)) >= Math.abs(velocityY)) {
				velocityY = 0.0d;
			} else {
				velocityY -= dv * velocityY / Math.hypot(velocityX, velocityY);
			}

			displacementX = oldVelocityX * ServerGameController.MODEL_TICK;
			displacementY = oldVelocityY * ServerGameController.MODEL_TICK;

		}
		
		if (notRotating && wantToFire) {
			double missileVelocity = this.missileVelocity; // Missile is a bit faster than tank
			if (moving) {
				// FIXME: this will increase missile speed even if a tank is stuck to a wall :D
				missileVelocity += Math.hypot(velocityX, velocityY);
			}
			
			if (player.getMissilesCount() == 0) {
				ServerMissileController newMissile = new ServerMissileController(crashListener, collider, getPlayerUnitModel().getPosX(), getPlayerUnitModel().getPosY(), angle, missileVelocity);
				player.addMissile(newMissile);
			}
			wantToFire = false;
		}

		// Saving the actual position as "old"
		this.oldX = getPlayerUnitModel().getPosX();
		this.oldY = getPlayerUnitModel().getPosY();

		// Calculating the movement taking collisions into account
		DeltaXY dxy = new DeltaXY(displacementX, displacementY);
		boolean collided = !safeMove(dxy);
	
		// Calculating real displacement after all collisions
		double realDeltaX = Math.abs(getPlayerUnitModel().getPosX() - oldX);
		double realDeltaY = Math.abs(getPlayerUnitModel().getPosY() - oldY);

		if (collided) {
			// If we collided, we lose the velocity in this direction
			if (Math.abs(realDeltaY - Math.abs(displacementY)) > 0) {
				if (Math.abs(realDeltaX - Math.abs(displacementX)) > Math.abs(realDeltaY - Math.abs(displacementY))) {
					velocityX = 0.0;
				} else {
					velocityY = 0.0;
				}
			} else if (Math.abs(realDeltaX - Math.abs(displacementX)) > 0) {
				velocityX = 0.0;
			}
		}
		
		if (moving) {
			CollisionResult treesCollisionResult = collider.getIntersectionDepth(player.getUnit(), new BoxActivityCriterion() {
				
				@Override
				public boolean isActive(Box box) {
					if (box instanceof ServerCellController) {
						return ((ServerCellController)box).getCellModel().getType() == CellType.TREE;
					} else {
						return box.equals(player.getUnit());
					}
				}
			});
			
			for (BoxConstruction<?> tree : treesCollisionResult.targets.keySet()) {
				((ServerCellController)tree).setMovementTouchMoment(System.currentTimeMillis());
			}
		}
		
		getPlayerUnitModel().setMoving(moving);
		getPlayerUnitModel().setAngle(angle);
	}

	public boolean isInvincible() {
		double timeSec = (double)System.currentTimeMillis() / 1000;
		return (timeSec - getPlayerUnitModel().getSpawnMoment()) < SPAWN_INVINCIBILITY_DURATION; 
	}

}