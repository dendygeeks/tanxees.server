package bfbc.tank.core;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import bfbc.tank.core.api.Player.Appearance;
import bfbc.tank.core.api.Player.UnitType;

public class GlobalServices {
	private static Gson gson;
	
	static {
    	GsonBuilder builder = new GsonBuilder();
    	builder.excludeFieldsWithoutExposeAnnotation();
    	//builder.setPrettyPrinting();
    	
    	builder.registerTypeAdapter(ServerCell.class, new ServerCellTypeAdapter());
    	builder.registerTypeAdapter(ServerPlayer.Appearance.class, new AppearanceTypeAdapter());
    	builder.registerTypeAdapter(ServerPlayer.UnitType.class, new UnitTypeTypeAdapter());
    	
    	gson = builder.create();
	}
	
	static class AppearanceTypeAdapter extends com.google.gson.TypeAdapter<Appearance> {
		public Appearance read(JsonReader reader) throws IOException {
			// TODO It can't read
			return null;
		}

		public void write(JsonWriter writer, Appearance value) throws IOException {
			if (value == null) {
				writer.nullValue();
				return;
			}
			String name = value.id;
			writer.value(name);
		}
	}
	
	static class UnitTypeTypeAdapter extends com.google.gson.TypeAdapter<UnitType> {
		public UnitType read(JsonReader reader) throws IOException {
			// TODO It can't read
			return null;
		}

		public void write(JsonWriter writer, UnitType value) throws IOException {
			if (value == null) {
				writer.nullValue();
				return;
			}
			String name = value.id;
			writer.value(name);
		}
	}
	
	static class ServerCellTypeAdapter extends com.google.gson.TypeAdapter<ServerCell> {
		public ServerCell read(JsonReader reader) throws IOException {
			// TODO It can't read
			/*if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}
			String name = reader.nextString();
			return Cell.valueOf(name);*/
			return null;
		}

		public void write(JsonWriter writer, ServerCell value) throws IOException {
			if (value == null) {
				writer.nullValue();
				return;
			}
			String name = value.getType().code;
			writer.value(name);
		}
	}
	
	public static Gson getGson() {
		return gson;
	}
}
