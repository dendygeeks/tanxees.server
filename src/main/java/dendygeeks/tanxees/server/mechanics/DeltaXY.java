package dendygeeks.tanxees.server.mechanics;
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
	
	private static double maxWithSign(double a, double b) {
		if (a == 0) {
			return b;
		} else if (a > 0) { 
			return Math.max(a, b);
		} else {
			return Math.min(a, b);
		}
	}
	
	public static DeltaXY maxSameSign(DeltaXY a, DeltaXY b) {
		if (a.x * b.x >= 0 && a.y * b.y >= 0) {
			return new DeltaXY(maxWithSign(a.x, b.x), maxWithSign(a.y, b.y));
		} else {
			return null;
		}
	}
}
