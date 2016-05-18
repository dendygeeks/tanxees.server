package bfbc.tank.core;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import bfbc.tank.api.interfaces.Appearance;
import bfbc.tank.api.interfaces.UnitType;
import bfbc.tank.api.model.CellModel;

public class GlobalServices {
	private static Gson gson;
	
	static {
    	GsonBuilder builder = new GsonBuilder();
    	builder.excludeFieldsWithoutExposeAnnotation();
    	//builder.setPrettyPrinting();
    	
    	builder.registerTypeAdapter(CellModel.class, new CellTypeAdapter());
    	builder.registerTypeAdapter(Appearance.class, new AppearanceTypeAdapter());
    	builder.registerTypeAdapter(UnitType.class, new UnitTypeTypeAdapter());
    	
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
	
	static class CellTypeAdapter extends com.google.gson.TypeAdapter<CellModel> {
		public CellModel read(JsonReader reader) throws IOException {
			// TODO It can't read
			/*if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}
			String name = reader.nextString();
			return Cell.valueOf(name);*/
			return null;
		}

		public void write(JsonWriter writer, CellModel value) throws IOException {
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
