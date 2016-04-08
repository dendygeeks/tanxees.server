package bfbc.tank.core.mechanics;

public enum DeltaAngle {
	ZERO(0), PI_BY_2(90), PI(180), THREE_PI_BY_2(270);
	
	public final int angle;
	
	DeltaAngle(int angle) {
		this.angle = angle;
	}
	
	public static DeltaAngle fromAngle(int angle) {
		for (DeltaAngle da : values()) {
			if (da.angle == angle) return da;
		}
		return null;
	}
	
	public DeltaAngle inverse() {
		switch (this) {
		case ZERO:
			return ZERO;
		case PI_BY_2:
			return THREE_PI_BY_2;
		case PI:
			return PI;
		case THREE_PI_BY_2:
			return PI_BY_2;
		default:
			throw new RuntimeException("Invalid case");
		}
	}
}
