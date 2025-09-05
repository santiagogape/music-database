package app.control.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TempFolder {
    private final Path parent;

    public TempFolder(Path parent) {
        this.parent = parent;
    }

    public Path existsOrCreate(){
        Path temp = parent.resolve("temp");
        if (Files.exists(temp)) return temp;
        try {
            return Files.createDirectory(temp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
