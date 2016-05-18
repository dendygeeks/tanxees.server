package bfbc.tank.core.model;

import com.google.gson.annotations.Expose;

import bfbc.tank.core.api.DebugData;

public class DebugDataModel implements DebugData {

	@Expose
	private final String svg;
	
	public DebugDataModel(String svg) {
		this.svg = svg;
	}

	public String getSvg() {
		return svg;
	}
}
