package dendygeeks.tanxees.server.mechanics;

public interface Box {
	double getLeft();
	double getTop();
	double getRight();
	double getBottom();
	
	void move(DeltaXY delta);
	
	public interface BoxActivityCriterion {
		boolean isActive(Box box);
	}
	
	public static IntersectionResult getIntersectionDepth(Box box1, Box box2, BoxActivityCriterion activityCriterion) {
		if (!activityCriterion.isActive(box1) || !activityCriterion.isActive(box2)) return null;
		
		double c1x = (box1.getLeft() + box1.getRight()) / 2;
		double c1y = (box1.getTop() + box1.getBottom()) / 2;
		double d1x = box1.getRight() - c1x;
		double d1y = box1.getBottom() - c1y;
		
		double c2x = (box2.getLeft() + box2.getRight()) / 2;
		double c2y = (box2.getTop() + box2.getBottom()) / 2;
		double d2x = box2.getRight() - c2x;
		double d2y = box2.getBottom() - c2y;
		
		if ((Math.abs(c2x - c1x) >= d1x + d2x) || (Math.abs(c2y - c1y) >= d1y + d2y)) {
			return null;
		} else {
			double xOverlap = Math.max(0, Math.min(box1.getRight(), box2.getRight()) - Math.max(box1.getLeft(), box2.getLeft())),
		           yOverlap = Math.max(0, Math.min(box1.getBottom(), box2.getBottom()) - Math.max(box1.getTop(), box2.getTop()));
			
			double area = xOverlap * yOverlap;
			
			return new IntersectionResult(d1x + d2x - Math.abs(c2x - c1x), d1y + d2y - Math.abs(c2y - c1y), area);
		}
	}

}
