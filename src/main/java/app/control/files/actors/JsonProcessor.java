package app.control.files.actors;

import java.nio.file.Path;

public interface JsonProcessor {
    public <T> void write(T value, Path path );
    public <T> T read(Class<T> tClass, Path path);
    public <T> String toJson(T value);
    public <T> T fromJson(String value, Class<T> tClass);
}
