package dendygeeks.tanxees.server;

import com.google.gson.annotations.Expose;

import dendygeeks.tanxees.api.java.interfaces.Direction;

public class SpawnConfig {
	@Expose
	public final Direction direction;
	@Expose
	public final PointIJ spawnPoint;
	
	public SpawnConfig(PointIJ spawnPoint, Direction direction) {
		this.spawnPoint = spawnPoint;
		this.direction = direction;
	}
	public double getPosX(double cellSize) {
		return cellSize * (spawnPoint.i + 0.5);
	}
	public double getPosY(double cellSize) {
		return cellSize * (spawnPoint.j + 0.5);
	}
	
}