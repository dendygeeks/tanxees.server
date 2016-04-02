package bfbc.tank.core.mechanics;

public enum DeltaAngle {
	ZERO, PI_BY_2, PI, THREE_PI_BY_2;
	
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
