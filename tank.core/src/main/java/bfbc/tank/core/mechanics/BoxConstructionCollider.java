package bfbc.tank.core.mechanics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BoxConstructionCollider<T extends Box> {

	public interface CollisionFriendship<T extends Box> {
		boolean canCollide(BoxConstruction<T> con1, BoxConstruction<T> con2);
	}
	
	public class CollisionResult {
		public final Map<BoxConstruction<T>, IntersectionResult> targets;
		public CollisionResult(Map<BoxConstruction<T>, IntersectionResult> targets) {
			this.targets = Collections.unmodifiableMap(new LinkedHashMap<BoxConstruction<T>, IntersectionResult>(targets));
		}
		
		public IntersectionResult mostAggressiveIntersection() {
			if (targets.isEmpty()) return null;
			return targets.get(targets.keySet().iterator().next());
		}
	}
	public class MoveResult {
		public final DeltaXY delta;
		public final Map<BoxConstruction<T>, IntersectionResult> targets;
		public MoveResult(DeltaXY delta, Map<BoxConstruction<T>, IntersectionResult> targets) {
			this.delta = delta;
			this.targets = Collections.unmodifiableMap(new LinkedHashMap<BoxConstruction<T>, IntersectionResult>(targets));
		}

		public BoxConstruction<T> mostAggressiveIntersectionTarget() {
			if (targets.isEmpty()) return null;
			return targets.keySet().iterator().next();
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
		//IntersectionResult res = null; 
		BoxConstruction<T>[] agArr = agents.toArray(new BoxConstruction[] { });
		
		HashMap<BoxConstruction<T>, IntersectionResult> targets = new HashMap<>();
		for (int i = 0; i < agArr.length; i++) {
			if (agArr[i] != con && (friendship == null || friendship.canCollide(agArr[i], con))) {
				IntersectionResult d = BoxConstruction.getIntersectionDepth(agArr[i], con);
				if (d != null) {
					targets.put(agArr[i], d);
				}
				/*	if (res == null) {
						res = d;
					} else {
						res = IntersectionResult.max(res, d);
					}
				}*/
			}
		}
		
		// Sorting targets by area lowering criteria
		List<BoxConstruction<T>> boxCons = new ArrayList<>(targets.keySet());
		
		boxCons.sort(new Comparator<BoxConstruction<T>>() {
			@Override
			public int compare(BoxConstruction<T> o1, BoxConstruction<T> o2) {
				return targets.get(o1).area < targets.get(o2).area ? 1 : -1; 
			}
		});
		
		LinkedHashMap<BoxConstruction<T>, IntersectionResult> sortedTargets = new LinkedHashMap<>();
		for (BoxConstruction<T> boxCon : boxCons) {
			sortedTargets.put(boxCon, targets.get(boxCon));
		}
		
		return new CollisionResult(sortedTargets);
	}
	
	public synchronized MoveResult tryMove(BoxConstruction<T> con, DeltaXY delta) {
		CollisionResult before = getIntersectionDepth(con);
		if (before.targets.size() > 0) throw new RuntimeException("Invalid state before movement");
		con.move(delta);
		CollisionResult after = getIntersectionDepth(con);
		DeltaXY deltaRes;
		if (after.targets.size() == 0) {
			deltaRes = delta;
		} else {
			IntersectionResult mostAggressive = after.mostAggressiveIntersection();
			
			double lenY = (Math.abs(delta.y) - mostAggressive.depthY) / Math.abs(delta.y);
			double lenX = (Math.abs(delta.x) - mostAggressive.depthX) / Math.abs(delta.x);
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
