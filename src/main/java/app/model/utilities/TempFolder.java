package app.model.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record TempFolder(Path parent) {

    public Path existsOrCreate() {
        Path path = path();
        if (Files.exists(path)) return path;
        try {
            return Files.createDirectory(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path path(){
        return parent.resolve("temp");
    }

}
