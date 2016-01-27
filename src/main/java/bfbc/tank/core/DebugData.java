package bfbc.tank.core;

import com.google.gson.annotations.Expose;

public class DebugData {
	@Expose
	private final String svg;
	
	public DebugData(String svg) {
		this.svg = svg;
	}

	public String getSvg() {
		return svg;
	}
}
