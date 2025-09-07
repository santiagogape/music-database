package app.control.files.actors;

public interface JsonWriter {
    <T> void save(T value, String path);
}
