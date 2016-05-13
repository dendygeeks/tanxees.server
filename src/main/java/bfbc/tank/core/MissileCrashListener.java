package bfbc.tank.core;

import java.util.Collection;

import bfbc.tank.core.mechanics.BoxConstruction;

public interface MissileCrashListener {
	void missileCrashed(ServerMissile missile, Collection<BoxConstruction<?>> targets);
}
