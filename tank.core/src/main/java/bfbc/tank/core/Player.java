package bfbc.tank.core;

import java.util.HashMap;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class Player extends GameObject {
	
	private static final double SIZE = 36;
	
	private static HashMap<Direction, Double> DIRECTION_ANGLES;
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

	private BoxConstructionCollider<Box> collider;
	
	public boolean isMoving() {
		return moving;
	}

	private PlayerCommand activeCommand;
	
	private Direction direction;

	public Direction getDirection() {
		return direction;
	}
	
	public PlayerCommand getActiveCommand() {
		return activeCommand;
	}
	
	public void setActiveCommand(PlayerCommand activeCommand) {
		this.activeCommand = activeCommand;
	}
	
	public Player(Game game, BoxConstructionCollider<Box> collider, Direction direction, PlayerCommand activeCommand, boolean moving, double posX, double posY, double angle) {
		super(game, SIZE, SIZE, posX, posY, angle);
		this.collider = collider;
		this.activeCommand = activeCommand;
		this.direction = direction;
		this.moving = moving;
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
		double angleVel = 500;
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

		double velocity = 85.0d;
		double displacement = velocity * Game.MODEL_TICK;
		
		// If we are not rotating, we are moving
		if (notRotating && moving) {
			DeltaXY dxy;
			switch (direction) {
			case LEFT:
				dxy = new DeltaXY(-displacement, 0);
				break;
			case RIGHT:
				dxy = new DeltaXY(displacement, 0);
				break;
			case UP:
				dxy = new DeltaXY(0, -displacement);
				break;
			case DOWN:
				dxy = new DeltaXY(0, displacement);
				break;
			default:
				throw new RuntimeException("Strange direction case");
			}
			collider.tryMove(Player.this, dxy);
		}
		
		if (notRotating && wantToFire) {
			getGame().createMissile(this, getPosX(), getPosY(), getAngle(), velocity);
			wantToFire = false;
		}
	}
	
}