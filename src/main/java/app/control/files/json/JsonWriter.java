package app.control.files.json;

import java.nio.file.Path;

public interface JsonWriter {
    <T> void write(T value, Path path );

}
