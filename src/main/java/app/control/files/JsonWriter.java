package app.control.files;

public interface JsonWriter {
    <T> void save(T value, String path);
}
