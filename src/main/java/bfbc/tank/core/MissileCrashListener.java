package bfbc.tank.core;

import java.util.Collection;

import bfbc.tank.core.controllers.ServerMissileController;
import bfbc.tank.core.mechanics.BoxConstruction;

public interface MissileCrashListener {
	void missileCrashed(ServerMissileController missile, Collection<BoxConstruction<?>> targets);
}
