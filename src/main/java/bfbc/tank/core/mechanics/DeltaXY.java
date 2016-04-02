package bfbc.tank.core.mechanics;
public class DeltaXY {
	public final double x, y;

	public DeltaXY(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public DeltaXY inverse() {
		return new DeltaXY(-x, -y);
	}
	
	public double length() {
		return Math.pow(x*x + y*y, 0.5);
	}
	
	public DeltaXY mul(double d) {
		return new DeltaXY(x*d, y*d);
	}
}
