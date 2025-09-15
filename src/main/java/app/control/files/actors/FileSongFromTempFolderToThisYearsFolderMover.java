package app.control.files.actors;

import app.model.items.FileSong;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public record FileSongFromTempFolderToThisYearsFolderMover(Path tempFolder, Path thisYearFolder) {

    public List<FileSong> moveToThisYearsFolder(List<FileSong> fileSongs) {
        return fileSongs.stream().peek(this::move).map(this::updateDirectory).toList();
    }

    private FileSong updateDirectory(FileSong fileSong) {
        return new FileSong() {
            @Override
            public String title() {
                return fileSong.title();
            }

            @Override
            public String album() {
                return fileSong.album();
            }

            @Override
            public String artists() {
                return fileSong.artists();
            }

            @Override
            public String request() {
                return fileSong.request();
            }

            @Override
            public String directory() {
                return thisYearFolder.getFileName().toString();
            }

            @Override
            public LocalDateTime creation() {
                return fileSong.creation();
            }

            @Override
            public String nameWithExtension() {
                return fileSong.nameWithExtension();
            }

            @Override
            public Integer id() {
                return fileSong.id();
            }

            @Override
            public String name() {
                return fileSong.name();
            }

            @Override
            public ItemType type() {
                return ItemType.file;
            }
        };
    }

    private void move(FileSong f) {
        try {
            Files.move(tempFolder.resolve(f.nameWithExtension()), thisYearFolder.resolve(f.nameWithExtension()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
