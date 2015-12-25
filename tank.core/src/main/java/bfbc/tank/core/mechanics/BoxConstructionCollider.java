package bfbc.tank.core.mechanics;

import java.util.ArrayList;
import java.util.List;

public class BoxConstructionCollider<T extends Box> {

	private List<BoxConstruction<? extends T>> agents = new ArrayList<>();
	public BoxConstructionCollider() {
		
	}
	
	public void clearAgents() {
		agents.clear();
	}
	
	public void addAgent(BoxConstruction<? extends T> agent) {
		agents.add(agent);
	}
	
	public DepthXY getIntersectionDepth() {
		DepthXY res = null; 
		@SuppressWarnings("unchecked")
		BoxConstruction<T>[] agArr = agents.toArray(new BoxConstruction[] { }); 
		for (int i = 0; i < agArr.length - 1; i++) {
			for (int j = i + 1; j < agArr.length; j++) {
				DepthXY d = BoxConstruction.getIntersectionDepth(agArr[i], agArr[j]);
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
	
	public DeltaXY tryMove(BoxConstruction<T> con, DeltaXY delta) {
		DepthXY before = getIntersectionDepth();
		if (before != null) throw new RuntimeException("Invalid state before movement");
		con.move(delta);
		DepthXY after = getIntersectionDepth();
		if (after == null) {
			return delta;
		} else {
			double lenY = (Math.abs(delta.y) - after.y) / Math.abs(delta.y);
			double lenX = (Math.abs(delta.x) - after.x) / Math.abs(delta.x);
			double len = Math.max(lenX, lenY);
			DeltaXY dNew = delta.mul(len - 0.0001);
			con.move(delta.inv());
			con.move(dNew);
			return dNew;
		}
		
	}
}
