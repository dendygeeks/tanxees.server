package bfbc.tank.core;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.DebugData;

public class ServerDebugData implements DebugData {
	@Expose
	private final String svg;
	
	public ServerDebugData(String svg) {
		this.svg = svg;
	}

	public String getSvg() {
		return svg;
	}
}
