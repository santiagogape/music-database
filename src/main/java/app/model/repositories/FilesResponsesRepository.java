package app.model.repositories;

import app.model.items.FileReference;
import app.model.items.FileSong;
import app.model.items.Response;
import app.model.items.SimpleItem;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class FilesResponsesRepository {


    private final Set<Integer> fromAnotherSource;
    private final Map<String,Map<Integer,FileSong>> files;
    private final Map<String,Map<Response.Status,Map<Integer,Response>>> responses;

    public static FilesResponsesRepository from(List<FileSong> files, List<Response> responses, Set<Integer> fromAnotherSource){
        Map<String, Map<Integer, FileSong>> map = fileSongListToMapByDirectory(files);
        return new FilesResponsesRepository(map, responseListToMapByDirectory(map,responses), fromAnotherSource);
    }

    private static Map<String,Map<Integer,FileSong>> fileSongListToMapByDirectory(List<FileSong> fileSongs){
        return fileSongs.stream().collect(groupingBy(FileReference::directory,Collectors.toMap(SimpleItem::id,f->f)));
    }

    private static Map<String, Map<Response.Status, Map<Integer, Response>>> responseListToMapByDirectory(Map<String, Map<Integer, FileSong>> fileSongs, List<Response> responses){
        Map<String,Map<Response.Status,Map<Integer,Response>>> responseMap = new HashMap<>();

        fileSongs.forEach((directory,map)->{
            responseMap.put(directory, new HashMap<>());
            responseMap.get(directory).putAll(
                    responses.stream().filter(r->map.containsKey(r.id()))
                            .collect(Collectors.groupingBy(Response::status,Collectors.toMap(SimpleItem::id,r->r)))
            );
        });
        return responseMap;
    }

    public FilesResponsesRepository(
            Map<String,Map<Integer,FileSong>> files,
            Map<String,Map<Response.Status,Map<Integer,Response>>> responses,
            Set<Integer> fromAnotherSource) {
        this.files = files;
        this.responses = responses;
        this.fromAnotherSource = fromAnotherSource;
        checkStructure();
    }

    private List<FileSong> fileSongListOf(Map<String, Map<Integer, FileSong>> map) {
        return map.values()
                .stream()
                .flatMap(f->f.values().stream())
                .toList();
    }

    private Optional<FileSong> getFileSongFromResponse(Response response){
        return files.values().stream()
                .filter(integerFileSongMap -> integerFileSongMap.containsKey(response.id()))
                .map(integerFileSongMap -> integerFileSongMap.get(response.id())).findFirst();
    }

    private Optional<Response> getResponseFromFileSong(FileSong file) {
        return responses.values().stream()
                .flatMap(f->f.values().stream())
                .filter(f-> f.containsKey(file.id()))
                .map(f->f.get(file.id()))
                .findFirst();
    }
    
    private boolean isFromAnotherSource(Integer id){
        return fromAnotherSource.contains(id);
    }

    private Map<String, Map<Response.Status, Map<Integer, Response>>> mapResponseFromMapFileSong(Map<String, Map<Integer, FileSong>> map) {
        Map<String, Map<Response.Status, Map<Integer, Response>>> result = new HashMap<>();
        map.forEach((directory,fileSongs)->{
            if (responses.containsKey(directory)) {
                result.put(directory, new HashMap<>());
                fileSongs.values().stream()
                        .map(this::getResponseFromFileSong).filter(Optional::isPresent).map(Optional::get)
                        .forEach(r->{
                            result.get(directory).putIfAbsent(r.status(),new HashMap<>());
                            result.get(directory).get(r.status()).put(r.id(),r);
                        });
            }
        });
        return result;
    }

    public List<FileSong> filesWithoutResponse(){
        return  fileSongListOf(files).stream().filter(f -> getResponseFromFileSong(f).isEmpty()).collect(Collectors.toList());
    }

    public FilesResponsesRepository filterByDirectory(String directory){
        Map<String, Map<Integer, FileSong>> filteredFiles = new HashMap<>();
        Map<String,Map<Response.Status,Map<Integer,Response>>> filteredResponses = new HashMap<>();
        filteredFiles.put(directory,files.get(directory));
        filteredResponses.put(directory,responses.get(directory));
        return new FilesResponsesRepository(filteredFiles,
                filteredResponses,
                fileSongListOf(filteredFiles).stream().map(SimpleItem::id).filter(this::isFromAnotherSource).collect(Collectors.toSet()));
    }

    public FilesResponsesRepository filterByResponseStatus(Response.Status status){
        Map<String,Map<Integer,FileSong>> filteredFiles = new HashMap<>();
        Map<String,Map<Response.Status,Map<Integer,Response>>> filteredResponses = new HashMap<>();
        Set<Integer> filteredFromAnotherSource = new HashSet<>();

        responses.forEach((directory, mapByStatus)->{
            filteredResponses.put(directory,new HashMap<>());
            filteredResponses.get(directory).put(status,mapByStatus.get(status));
            filteredFiles.put(directory, new HashMap<>());
            mapByStatus.entrySet().stream()
                    .flatMap(e->e.getValue().values().stream().map(SimpleItem::id))
                    .peek(i-> {if (isFromAnotherSource(i)) filteredFromAnotherSource.add(i);})
                    .forEach(i->filteredFiles.get(directory).put(i,files.get(directory).get(i)));
        });

        return new FilesResponsesRepository(filteredFiles,filteredResponses,filteredFromAnotherSource);
    }

    public FilesResponsesRepository filterByResponseFromAnotherSource(){

        Map<String,Map<Integer,FileSong>> filteredFiles = new HashMap<>();
        Map<String,Map<Response.Status,Map<Integer,Response>>> filteredResponses = new HashMap<>();

        files.forEach((directory,map)->{
            map.entrySet().stream()
                    .filter(e -> isFromAnotherSource(e.getKey()))
                    .findFirst()
                    .ifPresent(e->{
                        filteredFiles.put(directory, new HashMap<>());
                        filteredResponses.put(directory, new HashMap<>());
                        responses.get(directory).forEach((status, mapByStatus)->{
                            if (mapByStatus.containsKey(e.getKey())) filteredResponses.get(directory).put(status,new HashMap<>());
                        });
                        });
        });
        fromAnotherSource.forEach(i->{
            files.forEach((directory,mapByDirectory)->{
                if (mapByDirectory.containsKey(i)) filteredFiles.get(directory).put(i,mapByDirectory.get(i));
            });
            responses.forEach((directory,statusMap)->{
                statusMap.forEach((status,map)->{
                    if (map.containsKey(i)) filteredResponses.get(directory).get(status).put(i, map.get(i));
                });
            });
        });

        return new FilesResponsesRepository(filteredFiles,filteredResponses,fromAnotherSource);

    }

    public Map<FileSong,Response> toMap(){
        return files.entrySet().stream()
                .flatMap(e->e.getValue().entrySet().stream())
                .map(f-> Map.entry(f.getValue(), getResponseFromFileSong(f.getValue())))
                .filter(e->e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e->e.getValue().get()));
    }


    public void addFile(String directory, FileSong fileSong){
        if (!files.containsKey(directory)) {
            files.put(directory,new HashMap<>());
            responses.put(directory,new HashMap<>());
            for (Response.Status value : Response.Status.values()) {
                responses.get(directory).put(value,new HashMap<>());
            }
        }
        files.get(directory).put(fileSong.id(),fileSong);
    }

    public void addResponse(FileSong fileSong, Response response){
        responses.get(fileSong.directory()).get(response.status()).put(response.id(),response);
    }

    public void updateResponse(FileSong fileSong, Response.Status previous, Response response){
        responses.get(fileSong.directory()).get(previous).remove(response.id());
        responses.get(fileSong.directory()).get(response.status()).put(response.id(),response);
    }

    private void checkStructure(){
        responses.forEach((directory, statusMap)-> {
            for (Response.Status value : Response.Status.values()) {
                responses.get(directory).putIfAbsent(value,new HashMap<>());
            }
        });
    }
}
