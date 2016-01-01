package bfbc.tank.core;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class Player implements Box, BoxConstruction<Box> {
	
	private static final double SIZE = 36;
	
	private static HashMap<Direction, Double> DIRECTION_ANGLES;
	static {
		DIRECTION_ANGLES = new HashMap<>();
		DIRECTION_ANGLES.put(Direction.RIGHT, 0d);
		DIRECTION_ANGLES.put(Direction.DOWN, 90d);
		DIRECTION_ANGLES.put(Direction.LEFT, 180d);
		DIRECTION_ANGLES.put(Direction.UP, 270d);
	}
	
	private Game game;
	
	@Expose
	private boolean moving;
	@Expose
	private double posX;
	@Expose
	private double posY;
	@Expose
	private double angle;
	
	private boolean wantToFire;

	private BoxConstructionCollider<Box> collider;
	
	public boolean isMoving() {
		return moving;
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
	
	public String toJson() {
		return GlobalServices.getGson().toJson(this);
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
		this.game = game;
		this.collider = collider;
		this.activeCommand = activeCommand;
		this.direction = direction;
		this.moving = moving;
		this.posX = posX;
		this.posY = posY;
		this.angle = angle;
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

		double angleDelta = DIRECTION_ANGLES.get(direction) - angle;
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
			game.createMissile(this, posX, posY, angle, velocity);
			wantToFire = false;
		}
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
	public Iterator<Box> iterator() {
		Iterator<Box> res = new Iterator<Box>() {
			
			private boolean hasNext = true;
			
			@Override
			public Box next() {
				hasNext = false;
				return Player.this;
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
		return res;
	}
	
	@Override
	public boolean isActive() {
		return true;
	}
}