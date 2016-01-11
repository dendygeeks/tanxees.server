package bfbc.tank.core.mechanics;

import java.util.ArrayList;
import java.util.List;

import bfbc.tank.core.Missile;

public class BoxConstructionCollider<T extends Box> {

	public interface CollisionFriendship<T extends Box> {
		boolean canCollide(BoxConstruction<T> con1, BoxConstruction<T> con2);
	}
	
	public class CollisionResult {
		public final DepthXY depth;
		public final BoxConstruction<T>[] targets;
		public CollisionResult(DepthXY depth, BoxConstruction<T>[] targets) {
			this.depth = depth;
			this.targets = targets;
		}
	}
	public class MoveResult {
		public final DeltaXY delta;
		public final BoxConstruction<T>[] targets;
		public MoveResult(DeltaXY delta, BoxConstruction<T>[] targets) {
			this.delta = delta;
			this.targets = targets;
		}
	}
	
	private List<BoxConstruction<? extends T>> agents = new ArrayList<>();
	private CollisionFriendship<T> friendship;
	
	public BoxConstructionCollider() {
		
	}
	
	public synchronized void clearAgents() {
		agents.clear();
	}
	
	public boolean containsAgent(BoxConstruction<? extends T> agent) {
		return agents.contains(agent);
	}
	
	public synchronized void addAgent(BoxConstruction<? extends T> agent) {
		agents.add(agent);
	}
	
	public void removeAgent(BoxConstruction<? extends T> agent) {
		agents.remove(agent);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized CollisionResult getIntersectionDepth(BoxConstruction<T> con) {
		DepthXY res = null; 
		BoxConstruction<T>[] agArr = agents.toArray(new BoxConstruction[] { });
		List<BoxConstruction<T>> targets = new ArrayList<>();
		for (int i = 0; i < agArr.length; i++) {
			if (agArr[i] != con && (friendship == null || friendship.canCollide(agArr[i], con))) {
				DepthXY d = BoxConstruction.getIntersectionDepth(agArr[i], con);
				if (d != null) {
					targets.add(agArr[i]);
					if (res == null) {
						res = d;
					} else {
						res = DepthXY.max(res, d);
					}
				}
			}
		}
		return new CollisionResult(res, targets.toArray(new BoxConstruction[] {}));
	}
	
	public synchronized MoveResult tryMove(BoxConstruction<T> con, DeltaXY delta) {
		CollisionResult before = getIntersectionDepth(con);
		if (before.targets.length > 0) throw new RuntimeException("Invalid state before movement");
		con.move(delta);
		CollisionResult after = getIntersectionDepth(con);
		DeltaXY deltaRes;
		if (after.targets.length == 0) {
			deltaRes = delta;
		} else {
			double lenY = (Math.abs(delta.y) - after.depth.y) / Math.abs(delta.y);
			double lenX = (Math.abs(delta.x) - after.depth.x) / Math.abs(delta.x);
			double len = Math.max(lenX, lenY);
			DeltaXY dNew = delta.mul(len - 0.0001);
			con.move(delta.inv());
			con.move(dNew);
			deltaRes = dNew;
		}
		return new MoveResult(deltaRes, after.targets);
	}
	
	public synchronized void setFriendship(CollisionFriendship<T> friendship) {
		this.friendship = friendship;
	}
}
