package bfbc.tank.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GlobalServices {
	private static Gson gson;
	
	static {
    	GsonBuilder builder = new GsonBuilder();
    	builder.excludeFieldsWithoutExposeAnnotation();
    	
    	builder.registerTypeAdapter(Cell.class, new Cell.TypeAdapter());
    	builder.registerTypeAdapter(Player.Appearance.class, new Player.Appearance.TypeAdapter());
    	
    	gson = builder.create();
	}
	
	public static Gson getGson() {
		return gson;
	}
}
