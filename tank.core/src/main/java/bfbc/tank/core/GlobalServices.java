package bfbc.tank.core;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class GlobalServices {
	private static Gson gson;
	
	private static class CellAdapter extends TypeAdapter<Cell> {
		public Cell read(JsonReader reader) throws IOException {
			// TODO It can't read
			/*if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}
			String name = reader.nextString();
			return Cell.valueOf(name);*/
			return null;
		}

		public void write(JsonWriter writer, Cell value) throws IOException {
			if (value == null) {
				writer.nullValue();
				return;
			}
			String name = value.getType().name();
			writer.value(name);
		}
	}
	
	static {
    	GsonBuilder builder = new GsonBuilder();
    	builder.excludeFieldsWithoutExposeAnnotation();
    	
    	builder.registerTypeAdapter(Cell.class, new CellAdapter());
    	
    	gson = builder.create();
	}
	
	public static Gson getGson() {
		return gson;
	}
}
