package app.model.repositories;

import app.model.items.FileSong;
import app.model.items.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilesResponsesRepository {

    private final Map<Integer, FileSong> files;
    private final Map<Integer, Response> responses;

    public FilesResponsesRepository(Map<Integer, FileSong> files, Map<Integer, Response> responses) {
        this.files = files;
        this.responses = responses;
    }

    public List<FileSong> getFiles() {
        return files.values().stream().toList();
    }

    public List<Response> getResponses() {
        return responses.values().stream().toList();
    }

    public List<FileSong> filesWithoutResponse(){
        return  getFiles().stream().filter(f -> !responses.containsKey(f.id())).toList();
    }

    public Map<FileSong, Response> currentPairs(){
        return files.values().stream()
                .filter(f -> responses.containsKey(f.id()))
                .collect(Collectors.toMap(f->f,f->responses.get(f.id())));
    }

    public FileSong getFileSongFromResponse(Response response){
        return files.get(response.id());
    }

    public Response getResponseFromFileSong(FileSong file) {
        return responses.get(file.id());
    }

    public void addFile(FileSong file){
        files.put(file.id(), file);
    }

    public void addResponse(Response response){
        responses.put(response.id(), response);
    }
}
