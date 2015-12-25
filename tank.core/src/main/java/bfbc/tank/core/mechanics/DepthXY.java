package bfbc.tank.core.mechanics;
public class DepthXY {
	public final double x, y;

	public DepthXY(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public static DepthXY max(DepthXY d1, DepthXY d2) {
		return new DepthXY(Math.max(d1.x, d2.x), Math.max(d1.y, d2.y));
	}
	
	public double length() {
		return Math.pow(x*x + y*y, 0.5);
	}

}