package bfbc.tank.core;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.mechanics.Box;
import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.BoxConstructionCollider;
import bfbc.tank.core.mechanics.DeltaXY;

public class Player implements Box, BoxConstruction<Box>  {
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
	@Expose
	private double posX;
	@Expose
	private double posY;
	@Expose
	private double angle;

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
	
	public Player(BoxConstructionCollider<Box> collider, Direction direction, PlayerCommand activeCommand, boolean moving, double posX, double posY, double angle) {
		this.collider = collider;
		this.activeCommand = activeCommand;
		this.direction = direction;
		this.moving = moving;
		this.posX = posX;
		this.posY = posY;
		this.angle = angle;
	}
	
	public void frameStep(double dt) {
		// Initiating boxes
		//BoxConstructionCollider<Box>
		
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
		
		double angleDelta = DIRECTION_ANGLES.get(direction) - angle;
		if (angleDelta < -180) angleDelta += 360;
		if (angleDelta > 180) angleDelta -= 360;
		
		boolean notRotating = false;
		double angleVel = 500;
		double angleSmall = angleVel / 50;
		if (angleDelta > angleSmall) {
			angle += angleVel * dt;
			moving = true;
		} else if (angleDelta < -angleSmall) {
			angle -= angleVel * dt;
			moving = true;
		} else {
			angle = DIRECTION_ANGLES.get(direction);
			notRotating = true;
		}

		double vel = 85d;
		
		// If we are not rotating, we are moving
		if (notRotating && moving) {
			if (direction == Direction.LEFT) {
				collider.tryMove(Player.this, new DeltaXY(-dt * vel, 0));
				//posX -= dt * vel;
			} else if (direction == Direction.RIGHT) {
				collider.tryMove(Player.this, new DeltaXY(dt * vel, 0));
				//posX += dt * vel;
			} else if (direction == Direction.UP) {
				collider.tryMove(Player.this, new DeltaXY(0, -dt * vel));
				//posY -= dt * vel;
			} else if (direction == Direction.DOWN) {
				collider.tryMove(Player.this, new DeltaXY(0, dt * vel));
				//posY += dt * vel;
			}
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