package dendygeeks.tanxees.server.mechanics;

import dendygeeks.tanxees.api.java.interfaces.DeltaAngle;
import dendygeeks.tanxees.server.mechanics.Box.BoxActivityCriterion;

public interface BoxConstruction<T extends Box> extends Iterable<T> {
	public void rotate(DeltaAngle delta);
	public void move(DeltaXY delta);
	
	public double getExcentricity();
	
	public static IntersectionResult getIntersectionDepth(BoxConstruction<?> con1, BoxConstruction<?> con2, BoxActivityCriterion activityCriterion) {
		IntersectionResult res = null; 
		for (Box b1 : con1) {
			for (Box b2 : con2) {
				IntersectionResult d = Box.getIntersectionDepth(b1, b2, activityCriterion);
				if (d != null) {
					if (res == null) {
						res = d;
					} else {
						res = IntersectionResult.max(res, d);
					}
				} 
			}
		}
		return res;
	}
	
}
