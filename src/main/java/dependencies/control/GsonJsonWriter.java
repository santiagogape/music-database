package dependencies.control;

import app.control.files.actors.JsonWriter;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;

public class GsonJsonWriter implements JsonWriter {
    private final Gson gson;

    public GsonJsonWriter(Gson prettyPrintingGson){
        this.gson = prettyPrintingGson;
    }

    @Override
    public <T> void save(T value, String path) {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(value, writer);
            System.out.println("✅ JSON generado en: " + path);
        } catch (IOException e) {
            System.err.println("❌ Error guardando JSON: " + e.getMessage());
        }
    }
}
