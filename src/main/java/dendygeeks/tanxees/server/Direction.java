package dendygeeks.tanxees.server;

import dendygeeks.tanxees.server.mechanics.DeltaAngle;

public enum Direction {
	RIGHT(0), DOWN(90), LEFT(180), UP(270);
	
	public final int angle;
	
	Direction(int angle) {
		this.angle = angle;
	}
	
	public static DeltaAngle sub(Direction d1, Direction d2) {
		int a = (d1.angle - d2.angle + 360) % 360;
		return DeltaAngle.fromAngle(a);
	}
}
