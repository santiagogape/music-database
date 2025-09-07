package app.control.files.actors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MP3PathsReader {
    public static List<Path> fromFolder(Path folder){
        List<Path> result = new ArrayList<>();
        try (Stream<Path> list = Files.list(folder)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".mp3"))
        ) {
            list.forEach(result::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
