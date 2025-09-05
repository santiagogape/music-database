package app.model.utilities.filesMetadata;

import java.util.List;

public record FolderMetadata(String folder, List<Mp3Metadata> files) { }
