package bfbc.tank.core;

import bfbc.tank.core.mechanics.BoxConstruction;

public interface MissileCrashListener {
	void missileCrashed(Missile me, BoxConstruction<?> target);
}
