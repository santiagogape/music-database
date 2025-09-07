package app.control.files.actors;

import app.model.utilities.filesMetadata.Mp3Metadata;

import java.nio.file.Path;
import java.util.List;

public interface MP3MetadataReader {

    List<Mp3Metadata> fromFolder(Path path);
}
