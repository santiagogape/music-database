package dependencies.jaudiotagger;

import java.util.List;

public class FolderMetadata {
    String folder;
    List<Mp3Metadata> files;

    public FolderMetadata(String folder, List<Mp3Metadata> files) {
        this.folder = folder;
        this.files = files;
    }
}
