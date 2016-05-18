package bfbc.tank.core;

import bfbc.tank.core.model.DebugDataModel;

public class ServerDebugDataController {
	private DebugDataModel debugDataModel;
	
	public ServerDebugDataController(DebugDataModel debugDataModel) {
		this.debugDataModel = debugDataModel;
	}
	
	public ServerDebugDataController() {
		this.debugDataModel = new DebugDataModel("");
	}

	public DebugDataModel getDebugDataModel() {
		return debugDataModel;
	}
}
