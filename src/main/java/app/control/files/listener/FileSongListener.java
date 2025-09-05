package app.control.files.listener;

import app.model.items.FileSong;

import java.util.List;

public interface FileSongListener {
    void receive(List<FileSong> fileSongs);
    List<FileSong> consume();
}
