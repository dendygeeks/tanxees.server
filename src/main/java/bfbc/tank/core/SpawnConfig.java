package bfbc.tank.core;

public class SpawnConfig {
	public final Direction direction;
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