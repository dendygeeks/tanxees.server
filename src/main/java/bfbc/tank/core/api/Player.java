package bfbc.tank.core.api;

import java.io.IOException;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public interface Player {
	enum Appearance {

		GREEN("green"), YELLOW("yellow"), GRAY("gray");



		public final String id;

		Appearance(String id) {
			this.id = id;
		}
	}
	
	enum UnitType {

		SMALL("small", 20/*22*/, 27), MEDIUM("medium", 34, 34);

		public final String id;
		public final double sizeW, sizeL;

		UnitType(String id, double sizeW, double sizeL) {
			this.id = id;
			this.sizeW = sizeW;
			this.sizeL = sizeL;
		}
	}
	
	public UnitType getUnitType();
	public PlayerUnit getUnit();
	public Appearance getAppearance();
	public int getFrags();
	public DebugData getDebugData();
	public List<Missile> getMissiles();

}
