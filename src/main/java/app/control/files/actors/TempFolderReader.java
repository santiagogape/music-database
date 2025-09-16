package app.control.files.actors;

import app.control.files.actors.mp3Metadata.MP3Processor;
import app.model.items.FileSong;
import app.model.resources.FilesFromDirectory;

import java.util.List;

public class TempFolderReader {

    private final MP3Processor reader;
    private final FileSongFromTempFolderToThisYearsFolderMover mover;

    public TempFolderReader(MP3Processor reader, FileSongFromTempFolderToThisYearsFolderMover mover) {
        this.reader = reader;
        this.mover = mover;
    }

    public FilesFromDirectory readTemp(){
        System.out.println("reading from temp");
        System.out.println("to:" +mover.thisYearFolder().getFileName().toString());
        System.out.println(mover.tempFolder());
        List<FileSong> process = reader.process(mover.tempFolder());
        System.out.println("processed: "+process);
        List<FileSong> fileSongs = mover.moveToThisYearsFolder(process);
        System.out.println("moved:"+fileSongs);
        return new FilesFromDirectory(mover.thisYearFolder().getFileName().toString(),true, fileSongs);
    }
}
