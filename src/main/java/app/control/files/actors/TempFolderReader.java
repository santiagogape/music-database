package app.control.files.actors;

import app.control.files.actors.mp3Metadata.MP3Processor;
import app.model.resources.FilesFromDirectory;

public class TempFolderReader {

    private final MP3Processor reader;
    private final FileSongFromTempFolderToThisYearsFolderMover mover;

    public TempFolderReader(MP3Processor reader, FileSongFromTempFolderToThisYearsFolderMover mover) {
        this.reader = reader;
        this.mover = mover;
    }

    public FilesFromDirectory readTemp(){
        return new FilesFromDirectory(mover.thisYearFolder().getFileName().toString(),true,
                mover.moveToThisYearsFolder(reader.process(mover.thisYearFolder()))
                );
    }
}
