package app.model.resources;

import app.model.items.FileSong;
import app.model.items.Response;

public record FileSongStatusToAddedResponse(FileSong fileSong, Response.Status previous, Response updated) {
}
