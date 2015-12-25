package bfbc.tank.core.mechanics;

public interface BoxConstruction<T extends Box> extends Iterable<T> {
	public void move(DeltaXY delta);
	
	public static DepthXY getIntersectionDepth(BoxConstruction<?> con1, BoxConstruction<?> con2) {
		DepthXY res = null; 
		for (Box b1 : con1) {
			for (Box b2 : con2) {
				DepthXY d = Box.getIntersectionDepth(b1, b2);
				if (d != null) {
					if (res == null) {
						res = d;
					} else {
						res = DepthXY.max(res, d);
					}
				} 
			}
		}
		return res;
	}
	
}
