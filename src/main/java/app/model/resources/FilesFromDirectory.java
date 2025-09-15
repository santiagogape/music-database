package app.model.resources;

import app.model.items.FileSong;

import java.util.List;

//CLASS,RECORD,ENUM
public record FilesFromDirectory(String directory, boolean areNew, List<FileSong> files) {
}
