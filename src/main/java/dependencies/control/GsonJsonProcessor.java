package dependencies.control;

import app.control.files.json.JsonProcessor;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GsonJsonProcessor implements JsonProcessor {

    private final Gson gson;

    public GsonJsonProcessor(Gson gson) {
        this.gson = gson;
    }

    public <T> void write(T value, Path path ){
        try (FileWriter writer = new FileWriter(path.toFile())) {
            gson.toJson(value, writer);
            System.out.println("✅ JSON stored in: " + path);
        } catch (IOException e) {
            System.err.println("❌ Error storing JSON: " + e.getMessage());
        }
    }

    public <T> T read(Class<T> tClass, Path path){
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> String toJson(T value) {
        return gson.toJson(value);
    }

    @Override
    public <T> T fromJson(String value, Class<T> tClass) {
        return gson.fromJson(value,tClass);
    }
}
