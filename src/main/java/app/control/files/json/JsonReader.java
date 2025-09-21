package app.control.files.json;

import java.nio.file.Path;

public interface JsonReader {
    <T> T read(Class<T> tClass, Path path);

}
