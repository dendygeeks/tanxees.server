package bfbc.tank.core;

public interface MissileSpawner {
	Missile spawnMissile(PlayerUnit t, double posX, double posY, double angle, double velocity);
}
