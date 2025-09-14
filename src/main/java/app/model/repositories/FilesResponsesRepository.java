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
        Map<String, Map<Integer, FileSong>> collect = files.stream().collect(groupingBy(FileSong::directory, Collectors.toMap(FileSong::id, f -> f)));
        return from(collect, responses, fromAnotherSource);
    }

    private static FilesResponsesRepository from(Map<String, Map<Integer, FileSong>> collect, List<Response> responses, Set<Integer> fromAnotherSource) {
        return new FilesResponsesRepository(
                collect,
                responseListToMapByDirectory(collect,responses),
                fromAnotherSource
        );
    }

    public static Map<String,Map<Integer,FileSong>> fileSongListToMapByDirectory(List<FileSong> fileSongs){
        return fileSongs.stream().collect(groupingBy(FileReference::directory,Collectors.toMap(SimpleItem::id,f->f)));
    }

    public static Map<String, Map<Response.Status, Map<Integer, Response>>> responseListToMapByDirectory(Map<String, Map<Integer, FileSong>> fileSongs, List<Response> responses){
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
    }

    public Map<String,Map<Integer,FileSong>> getFiles() {
        return files;
    }
    public List<FileSong> fileSongList() {
        return fileSongListOf(files);
    }

    private List<FileSong> fileSongListOf(Map<String, Map<Integer, FileSong>> map) {
        return map.values()
                .stream()
                .flatMap(f->f.values().stream())
                .toList();
    }


    public Map<String,Map<Response.Status,Map<Integer,Response>>> getResponses() {
        return responses;
    }
    public List<Response> responseList(){
        return responseListOf(responses);
    }

    private List<Response> responseListOf(Map<String, Map<Response.Status, Map<Integer, Response>>> map) {
        return map.values()
                .stream()
                .flatMap(f->f.values().stream())
                .flatMap(f->f.values().stream())
                .toList();
    }

    public Optional<FileSong> getFileSongFromResponse(Response response){
        return files.values().stream()
                .filter(integerFileSongMap -> integerFileSongMap.containsKey(response.id()))
                .map(integerFileSongMap -> integerFileSongMap.get(response.id())).findFirst();
    }

    public Optional<Response> getResponseFromFileSong(FileSong file) {
        return responses.values().stream()
                .flatMap(f->f.values().stream())
                .filter(f-> f.containsKey(file.id()))
                .map(f->f.get(file.id()))
                .findFirst();
    }
    
    public boolean isFromAnotherSource(Integer id){
        return fromAnotherSource.contains(id);
    }

    public Set<Integer> getFromAnotherSource() {
        return fromAnotherSource;
    }

    public Map<FileSong,Response> toMap(){
        return files.entrySet().stream()
                .flatMap(e->e.getValue().entrySet().stream())
                .map(f-> Map.entry(f.getValue(), getResponseFromFileSong(f.getValue())))
                .filter(e->e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e->e.getValue().get()));
    }

    public FilesResponsesRepository filterByDirectory(String directory){
        Map<String, Map<Integer, FileSong>> map = new HashMap<>();
        files.entrySet().stream()
                .filter(f -> Objects.equals(f.getKey(), directory))
                .findFirst().ifPresent(e->map.put(e.getKey(),e.getValue()));
        return new FilesResponsesRepository(map,
                mapResponseFromMapFileSong(map),
                fileSongListOf(map).stream().map(SimpleItem::id).filter(this::isFromAnotherSource).collect(Collectors.toSet()));
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


    public FilesResponsesRepository filterByResponseStatus(Response.Status status){
        Map<String,Map<Response.Status,Map<Integer,Response>>> map = new HashMap<>();

        responses.forEach((directory, mapByStatus)->{
            if (mapByStatus.containsKey(status)) {
                map.put(directory,new HashMap<>());
                map.get(directory).put(status,mapByStatus.get(status));
            }
        });

        return new FilesResponsesRepository(
                responseListOf(map).stream().map(this::getFileSongFromResponse)
                        .filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.groupingBy(FileSong::directory,Collectors.toMap(FileSong::id,f->f))),
                map,
                responseListOf(map).stream().map(SimpleItem::id).filter(this::isFromAnotherSource).collect(Collectors.toSet())
        );
    }

    public FilesResponsesRepository filterByResponseFromAnotherSource(){

        List<Response> responseList = responseListOf(responses).stream().filter(r -> isFromAnotherSource(r.id())).toList();

        Map<String, Map<Integer, FileSong>> fileSongs = responseList.stream().map(this::getFileSongFromResponse)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.groupingBy(FileSong::directory, Collectors.toMap(FileSong::id, f -> f)));

        return from(fileSongs,responseList,responseList.stream().map(SimpleItem::id).collect(Collectors.toSet()));

    }
    

}
