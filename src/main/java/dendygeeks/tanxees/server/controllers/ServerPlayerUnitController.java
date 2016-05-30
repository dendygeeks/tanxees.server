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
	
	private double velocity = 85.0d;
	private double backVelocity = -50.0d;
	private double angleVel = 500;

	private double dragVelocity = 1.0 * velocity;	// From the ceiling

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
	
	public void respawnUnit() {
		getPlayerUnitModel().setSizeX(sizeForDir(false, sizeW, sizeL, spawnConfig.direction));
		getPlayerUnitModel().setSizeY(sizeForDir(true, sizeW, sizeL, spawnConfig.direction));
		getPlayerUnitModel().setPosX(spawnConfig.getPosX(cellSize));
		getPlayerUnitModel().setPosY(spawnConfig.getPosY(cellSize));
		getPlayerUnitModel().setAngle(DIRECTION_ANGLES.get(spawnConfig.direction));
		this.direction = spawnConfig.direction;
	}
	
	public ServerPlayerUnitController(ServerPlayerController player, double sizeW, double sizeL, double cellSize, BoxConstructionCollider collider, MissileCrashListener crashListener, SpawnConfig spawnConfig, PlayerKeysModel activeCommand, boolean moving, Double angle) {
		super(new PlayerUnitModel(sizeForDir(false, sizeW, sizeL, spawnConfig.direction), 
		      sizeForDir(true, sizeW, sizeL, spawnConfig.direction), 
		      spawnConfig.getPosX(cellSize), 
		      spawnConfig.getPosY(cellSize), 
		      angle != null ? angle : DIRECTION_ANGLES.get(spawnConfig.direction), false));
		this.player = player;
		this.cellSize = cellSize;
		this.collider = collider;
		this.crashListener = crashListener;
		this.activeCommand = activeCommand;
		this.direction = spawnConfig.direction;
		this.spawnConfig = spawnConfig;
		this.sizeW = sizeW;
		this.sizeL = sizeL;
	}

	public PlayerUnitModel getPlayerUnitModel() {
		return (PlayerUnitModel)getUnitModel();
	}
	
	public ServerPlayerUnitController(
			ServerPlayerController player, 
			double sizeW, double sizeL, 
			double cellSize, 
			BoxConstructionCollider collider, 
			MissileCrashListener crashListener, 
			SpawnConfig spawnConfig, 
			PlayerKeysModel activeCommand, 
			boolean moving) {
		this(player, sizeW, sizeL, cellSize, collider, crashListener, spawnConfig, activeCommand, moving, null);
	}

	private void safeMove(DeltaXY dxy) {
		BoxConstructionCollider.MoveRotateResult mr = collider.tryMove(this, dxy);
		for (BoxConstruction<?> t : mr.targets.keySet()) {
			if (t instanceof ServerMissileController) {
				crashListener.missileCrashed((ServerMissileController)t, Arrays.asList(new BoxConstruction[] { this }));
			}
		}
	}
	
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
			//if (targetFactor > 0) {	// We are only moving forward
				double velX = dragVelocity * targetFactor * oldDirX * rotationFactor,
				       velY = dragVelocity * targetFactor * oldDirY * rotationFactor;
	
				DeltaXY dxy = new DeltaXY(
						velX * ServerGameController.MODEL_TICK,
						velY * ServerGameController.MODEL_TICK
				);
				safeMove(dxy);
			//}
			
		}

		double displacement;
		if (!moveBackwards) { 
			displacement = velocity * ServerGameController.MODEL_TICK;
		} else {
			displacement = backVelocity * ServerGameController.MODEL_TICK;
		}
		
		if (notRotating && wantToFire) {
			double missileVelocity = velocity * 1.2; // Missile is a bit faster than tank
			if (moving) {
				missileVelocity += velocity;
			}
			
			if (player.getMissilesCount() == 0) {
				ServerMissileController newMissile = new ServerMissileController(crashListener, collider, getPlayerUnitModel().getPosX(), getPlayerUnitModel().getPosY(), angle, missileVelocity);
				player.addMissile(newMissile);
			}
			wantToFire = false;
		}

		// If we are not rotating, we are moving
		if (notRotating && moving) {
			DeltaXY dxy = new DeltaXY(displacement * Math.cos(angle / 180 * Math.PI),
					displacement * Math.sin(angle / 180 * Math.PI));
			safeMove(dxy);
		}
		
		if (moving) {
			CollisionResult treesCollisionResult = collider.getIntersectionDepth(player.getUnit(), new BoxActivityCriterion() {
				
				@Override
				public boolean isActive(Box box) {
					if (box instanceof ServerCellController) {
						return ((ServerCellController)box).getCellModel().getType() == CellType.TREE;
					} else {
						return false;
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

}