package bfbc.tank.core;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.port;
import static spark.Spark.staticFileLocation;
import static spark.Spark.webSocket;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

public class Main {
	private static final String STATIC_ROOT = "/" + Main.class.getPackage().getName().replace('.', '/') + "/root";
	
    public static void main(String[] args) {
    	port(9876);
    	staticFileLocation(STATIC_ROOT);

    	webSocket("/player", PlayerWebSocket.class);
    	 	
		get("/image/:name", (request, response) -> {
			
			String filePath = "data" + File.separator + request.params(":name");
			
			//response.type("image/jpeg");
			byte[] bytes = Files.readAllBytes(Paths.get(filePath));
			HttpServletResponse raw = response.raw();

			raw.getOutputStream().write(bytes);
			raw.getOutputStream().flush();
			raw.getOutputStream().close();

			return raw;
		});

		exception(IOException.class, (exception, request, response) -> {
			exception.printStackTrace();
			response.status(404);
			response.body("Input/Output exception occured: " + ((IOException)exception).getMessage());
		});
		
		init();
    }
}