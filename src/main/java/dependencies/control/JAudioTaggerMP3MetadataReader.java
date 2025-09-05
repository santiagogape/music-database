package dependencies.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import app.control.files.MP3MetadataReader;
import app.model.utilities.filesMetadata.Mp3Metadata;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;




public class JAudioTaggerMP3MetadataReader implements MP3MetadataReader {

    @Override
    public List<Mp3Metadata> fromFolder(Path folder) {
        List<Mp3Metadata> result = new ArrayList<>();
        if (Files.isDirectory(folder)) {
            try (Stream<Path> files = Files.list(folder)) {
                files.filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                        .forEach(p -> {
                            java.io.File file = p.toFile();
                            try {
                                AudioFile audioFile = AudioFileIO.read(file);
                                Tag tag = audioFile.getTag();

                                String title   = tag != null ? tag.getFirst(org.jaudiotagger.tag.FieldKey.TITLE)  : "";
                                String album   = tag != null ? tag.getFirst(org.jaudiotagger.tag.FieldKey.ALBUM)  : "";
                                String artists = tag != null ? tag.getFirst(org.jaudiotagger.tag.FieldKey.ARTIST) : "";

                                result.add(new Mp3Metadata(file.getName(), title, album, artists));
                            } catch (CannotReadException | IOException | TagException | ReadOnlyFileException |
                                     InvalidAudioFrameException e) {
                                throw new RuntimeException(e);
                            }

                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(folder.getFileName() + " not a folder.");
        }
        return result;
    }
}
