package app.control.files.listener;

import app.model.items.FileSong;

import java.util.List;

public interface FileSongProcessor {
    List<FileSong> process(List<FileSong> fileSongs);
}
