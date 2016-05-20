package dendygeeks.tanxees.server;

import java.util.Collection;

import dendygeeks.tanxees.server.controllers.ServerMissileController;
import dendygeeks.tanxees.server.mechanics.BoxConstruction;

public interface MissileCrashListener {
	void missileCrashed(ServerMissileController missile, Collection<BoxConstruction<?>> targets);
}
