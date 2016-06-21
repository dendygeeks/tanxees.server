package dendygeeks.tanxees.server.mechanics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dendygeeks.tanxees.api.java.interfaces.Cell;
import dendygeeks.tanxees.api.java.interfaces.DeltaAngle;
import dendygeeks.tanxees.api.java.interfaces.Unit;
import dendygeeks.tanxees.server.controllers.ServerCellController;
import dendygeeks.tanxees.server.controllers.ServerFlagController;
import dendygeeks.tanxees.server.controllers.ServerUnitController;
import dendygeeks.tanxees.server.mechanics.Box.BoxActivityCriterion;

public class BoxConstructionCollider {

	public interface CollisionFriendship {
		boolean canCollide(BoxConstruction<?> con1, BoxConstruction<?> con2);
	}
	
	public class CollisionResult {
		public final Map<BoxConstruction<?>, IntersectionResult> targets;
		public CollisionResult(Map<BoxConstruction<?>, IntersectionResult> targets) {
			this.targets = new LinkedHashMap<BoxConstruction<?>, IntersectionResult>(targets);
		}

		public CollisionResult(CollisionResult other) {
			this(other.targets);
		}

		public IntersectionResult mostAggressiveIntersection() {
			if (targets.isEmpty()) return null;
			return targets.get(targets.keySet().iterator().next());
		}
		
		public void subtract(CollisionResult other) {
			for (BoxConstruction<?> key : other.targets.keySet()) {
				targets.remove(key);
			}
		}
		
	}
	public class MoveRotateResult {
		public final boolean success;
		public final DeltaXY delta;
		public final Map<BoxConstruction<?>, IntersectionResult> targets;
		public MoveRotateResult(boolean success, DeltaXY delta, Map<BoxConstruction<?>, IntersectionResult> targets) {
			this.success = success;
			this.delta = delta;
			this.targets = Collections.unmodifiableMap(new LinkedHashMap<BoxConstruction<?>, IntersectionResult>(targets));
		}

		public BoxConstruction<?> mostAggressiveIntersectionTarget() {
			if (targets.isEmpty()) return null;
			return targets.keySet().iterator().next();
		}
	}
	
	private class OwnedBoxActivityCriterion implements BoxActivityCriterion {
		private final Unit mainUnit;
		
		public OwnedBoxActivityCriterion(BoxConstruction<?> con) {
			if (con instanceof ServerUnitController) {
				mainUnit = ((ServerUnitController)con).getUnitModel();
			} else {
				mainUnit = null;
			}
		}
		
		@Override
		public boolean isActive(Box box) {
			if (box instanceof ServerCellController) {
				ServerCellController cellController = (ServerCellController)box;
				if (mainUnit != null) {
					return !cellController.getType().isPassableFor(mainUnit);
				} else {
					return cellController.getType().isWall();
				}
			} else if (box instanceof ServerFlagController) {
				ServerFlagController flagController = (ServerFlagController)box;
				return !flagController.isCrashed();
			} else {
				return true;
			}
		}
	}
	
	private List<BoxConstruction<?>> agents = new ArrayList<>();
	private CollisionFriendship friendship;
	
	public BoxConstructionCollider() {
		
	}
	
	public synchronized void clearAgents() {
		agents.clear();
	}
	
	public boolean containsAgent(BoxConstruction<?> agent) {
		return agents.contains(agent);
	}
	
	public synchronized void addAgent(BoxConstruction<?> agent) {
		agents.add(agent);
	}
	
	public void removeAgent(BoxConstruction<?> agent) {
		agents.remove(agent);
	}
	
	public synchronized CollisionResult getIntersectionDepth(BoxConstruction<?> con, BoxActivityCriterion activityCriterion) {
		//IntersectionResult res = null; 
		BoxConstruction<?>[] agArr = agents.toArray(new BoxConstruction[] { });
		
		HashMap<BoxConstruction<?>, IntersectionResult> targets = new HashMap<>();
		for (int i = 0; i < agArr.length; i++) {
			if (agArr[i] != con && (friendship == null || friendship.canCollide(agArr[i], con))) {
				IntersectionResult d = BoxConstruction.getIntersectionDepth(agArr[i], con, activityCriterion);
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
		List<BoxConstruction<?>> boxCons = new ArrayList<>(targets.keySet());
		
		boxCons.sort(new Comparator<BoxConstruction<?>>() {
			@Override
			public int compare(BoxConstruction<?> o1, BoxConstruction<?> o2) {
				return targets.get(o1).area < targets.get(o2).area ? 1 : -1; 
			}
		});
		
		LinkedHashMap<BoxConstruction<?>, IntersectionResult> sortedTargets = new LinkedHashMap<>();
		for (BoxConstruction<?> boxCon : boxCons) {
			sortedTargets.put(boxCon, targets.get(boxCon));
		}
		
		return new CollisionResult(sortedTargets);
	}

	private MoveRotateResult tryRotateSingle(BoxConstruction<?> con, DeltaAngle delta) {
		OwnedBoxActivityCriterion obstacleBoxActivityCriterion = new OwnedBoxActivityCriterion(con);
		
		CollisionResult before = getIntersectionDepth(con, obstacleBoxActivityCriterion);
		con.rotate(delta);
		CollisionResult after = getIntersectionDepth(con, obstacleBoxActivityCriterion);

		CollisionResult modAfter = new CollisionResult(after);
		modAfter.subtract(before);
		
		DeltaXY deltaRes;
		if (modAfter.targets.size() == 0) {
			deltaRes = new DeltaXY(0, 0);
		} else {
			// Trying to get out of the collision targets
			
			// Searching for the maximal intersection depth
			double maxDepthX = 0, maxDepthY = 0;
			double bloha = 0.01;
			for (IntersectionResult ir : modAfter.targets.values()) {
				maxDepthX = Math.max(maxDepthX, ir.depthX) + bloha;
				maxDepthY = Math.max(maxDepthY, ir.depthY) + bloha;
			}
			
			deltaRes = null;
			DeltaXY minDelta = null;
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (i != 0 || j != 0) {
						DeltaXY dr = new DeltaXY(maxDepthX * i, maxDepthY * j);
						con.move(dr);
						CollisionResult wentAway = getIntersectionDepth(con, obstacleBoxActivityCriterion);
						wentAway.subtract(before);
						if (wentAway.targets.size() == 0) {
							if (minDelta == null) { 
								minDelta = dr; 
							} else {
								minDelta = minDelta.length() < dr.length() ? minDelta : dr;
							}
							
						}
						con.move(dr.inverse());
					}
				}
			}
			
			// Let's check if we are trying to "leap" too far away
			if (minDelta != null && minDelta.length() < con.getExcentricity()) {
				deltaRes = minDelta;
			}
			
			if (deltaRes != null) {
				con.move(deltaRes);
			} else {
				con.rotate(delta.inverse());
			}
		}

		return new MoveRotateResult(deltaRes != null, deltaRes, modAfter.targets);
	}
	
	public MoveRotateResult tryRotate(BoxConstruction<?> con, DeltaAngle delta) {
		MoveRotateResult mrr;
		if (delta != DeltaAngle.ZERO) {
			if (delta == DeltaAngle.PI) {
				delta = DeltaAngle.PI_BY_2; // Direction is not important
				mrr = tryRotateSingle(con, delta);
				if (mrr.success) {
					// Rotating again
					mrr = tryRotateSingle(con, delta);
				}
			} else {
				mrr = tryRotateSingle(con, delta);
			}
		} else {
			// Nothing
			mrr = new MoveRotateResult(true, new DeltaXY(0, 0), new HashMap<>());
		}
		return mrr;
	}
	
	public synchronized MoveRotateResult tryMove(BoxConstruction<?> con, DeltaXY delta) {
		OwnedBoxActivityCriterion obstacleBoxActivityCriterion = new OwnedBoxActivityCriterion(con);

		CollisionResult before = getIntersectionDepth(con, obstacleBoxActivityCriterion);
		
		//if (before.targets.size() > 0) throw new RuntimeException("Invalid state before movement");
		con.move(delta);
		CollisionResult after = getIntersectionDepth(con, obstacleBoxActivityCriterion);
		
		CollisionResult modAfter = new CollisionResult(after);
		modAfter.subtract(before);
		
		DeltaXY deltaRes;
		boolean success = true;
		if (modAfter.targets.size() == 0) {
			deltaRes = delta;
		} else {
			IntersectionResult mostAggressive = modAfter.mostAggressiveIntersection();
			
			double lenY = (Math.abs(delta.y) - mostAggressive.depthY) / Math.abs(delta.y);
			double lenX = (Math.abs(delta.x) - mostAggressive.depthX) / Math.abs(delta.x);
			double len = Math.max(lenX, lenY);
			DeltaXY dNew = delta.mul(len - 0.0001);
			con.move(delta.inverse());
			con.move(dNew);
			deltaRes = dNew;
		}
		return new MoveRotateResult(success, deltaRes, modAfter.targets);
	}
	
	
	public synchronized void setFriendship(CollisionFriendship friendship) {
		this.friendship = friendship;
	}
}
