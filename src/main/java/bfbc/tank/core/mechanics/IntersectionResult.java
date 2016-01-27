package bfbc.tank.core.mechanics;
public class IntersectionResult {
	public final double depthX, depthY;
	public final double area;

	public IntersectionResult(double x, double y, double area) {
		this.depthX = x;
		this.depthY = y;
		this.area = area;
	}
	
	public static IntersectionResult max(IntersectionResult d1, IntersectionResult d2) {
		return new IntersectionResult(Math.max(d1.depthX, d2.depthX), Math.max(d1.depthY, d2.depthY), Math.max(d1.area, d2.area));
	}
	
	public double length() {
		return Math.pow(depthX*depthX + depthY*depthY, 0.5);
	}

}