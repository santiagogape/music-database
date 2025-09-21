package app.model.repositories;

import app.model.items.FileReference;
import app.model.items.FileSong;
import app.model.items.Response;
import app.model.items.SimpleItem;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class FilesResponsesRepository {


    private final Set<Integer> fromAnotherSource;
    private final Map<Integer,String> individuals;
    private final Map<String,Map<Integer,FileSong>> files;
    private final Map<String,Map<Response.Status,Map<Integer,Response>>> responses;

    public static FilesResponsesRepository from(List<FileSong> files, List<Response> responses, Set<Integer> fromAnotherSource, List<FileSong.Individual> individuals){
        Map<String, Map<Integer, FileSong>> map = fileSongListToMapByDirectory(files);
        return new FilesResponsesRepository(
                map,
                responseListToMapByDirectory(map,responses),
                fromAnotherSource,
                individuals.stream().collect(Collectors.toMap(FileSong.Individual::id, FileSong.Individual::trackId)));
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
            Set<Integer> fromAnotherSource,
            Map<Integer,String> individuals) {
        this.files = files;
        this.responses = responses;
        this.fromAnotherSource = fromAnotherSource;
        this.individuals = individuals;
        checkStructure();
    }

    private List<FileSong> fileSongListOf(Map<String, Map<Integer, FileSong>> map) {
        return map.values()
                .stream()
                .flatMap(f->f.values().stream())
                .toList();
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

    private boolean isAnIndividual(Integer id){
        return individuals.containsKey(id);
    }


    public List<FileSong> filesWithoutResponse(){
        return  fileSongListOf(files).stream().filter(f -> getResponseFromFileSong(f).isEmpty()).collect(Collectors.toList());
    }

    public FilesResponsesRepository filterByDirectory(String directory){
        Map<String, Map<Integer, FileSong>> filteredFiles = new HashMap<>();
        Map<String,Map<Response.Status,Map<Integer,Response>>> filteredResponses = new HashMap<>();
        filteredFiles.put(directory,files.get(directory));
        filteredResponses.put(directory,responses.get(directory));
        Set<Integer> list = fileSongListOf(filteredFiles).stream().map(SimpleItem::id).collect(Collectors.toSet());
        return new FilesResponsesRepository(
                filteredFiles,
                filteredResponses,
                list.stream().filter(this::isFromAnotherSource).collect(Collectors.toSet()),
                individuals.entrySet().stream().filter(e->list.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue))
                );
    }

    public FilesResponsesRepository filterByResponseStatus(Response.Status status){
        Map<String,Map<Integer,FileSong>> filteredFiles = new HashMap<>();
        Map<String,Map<Response.Status,Map<Integer,Response>>> filteredResponses = new HashMap<>();
        Set<Integer> filteredFromAnotherSource = new HashSet<>();
        Map<Integer,String> filteredIndividuals = new HashMap<>();

        responses.forEach((directory, mapByStatus)->{
            filteredResponses.put(directory,new HashMap<>());
            filteredResponses.get(directory).put(status,mapByStatus.get(status));
            filteredFiles.put(directory, new HashMap<>());
            mapByStatus.entrySet().stream()
                    .flatMap(e->e.getValue().values().stream().map(SimpleItem::id))
                    .peek(i-> {if (isFromAnotherSource(i)) filteredFromAnotherSource.add(i);})
                    .peek(i->{if (isAnIndividual(i)) filteredIndividuals.put(i, individuals.get(i));})
                    .forEach(i->filteredFiles.get(directory).put(i,files.get(directory).get(i)));
        });

        return new FilesResponsesRepository(filteredFiles,filteredResponses,filteredFromAnotherSource, filteredIndividuals);
    }

    public FilesResponsesRepository filterByResponseFromAnotherSource(){
        Map<String, Map<Integer, FileSong>> filteredFiles = new HashMap<>();
        Map<String, Map<Response.Status, Map<Integer, Response>>> filteredResponses = new HashMap<>();
        filterFromSourceOrIndividual(this::isFromAnotherSource,filteredFiles,filteredResponses);
        return new FilesResponsesRepository(
                filteredFiles,
                filteredResponses,
                fromAnotherSource,
                new HashMap<>()
        );

    }

    private void filterFromSourceOrIndividual(
            Function<Integer,Boolean> function,
            Map<String, Map<Integer, FileSong>> filteredFiles,
            Map<String, Map<Response.Status, Map<Integer, Response>>> filteredResponses){
        files.forEach((directory, mapByDirectory) -> {
            Map<Integer, FileSong> filteredFileMap = new HashMap<>();
            Map<Response.Status, Map<Integer, Response>> filteredResponseStatusMap = new HashMap<>();

            mapByDirectory.forEach((id, fileSong) -> {
                if (function.apply(id)) {
                    filteredFileMap.put(id, fileSong);

                    responses.getOrDefault(directory, Map.of())
                            .forEach((status, responseMap) -> {
                                Response response = responseMap.get(id);
                                if (response != null) {
                                    filteredResponseStatusMap
                                            .computeIfAbsent(status, s -> new HashMap<>())
                                            .put(id, response);
                                }
                            });
                }
            });

            if (!filteredFileMap.isEmpty() || !filteredResponseStatusMap.isEmpty()) {
                filteredFiles.put(directory, filteredFileMap);
                filteredResponses.put(directory, filteredResponseStatusMap);
            }
        });
    }

    public FilesResponsesRepository filterByIndividuals(){
        Map<String, Map<Integer, FileSong>> filteredFiles = new HashMap<>();
        Map<String, Map<Response.Status, Map<Integer, Response>>> filteredResponses = new HashMap<>();
        filterFromSourceOrIndividual(this::isAnIndividual,filteredFiles,filteredResponses);
        return new FilesResponsesRepository(
                filteredFiles,
                filteredResponses,
                new HashSet<>(),
                individuals
        );
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

    public void addFromAnotherSource(Integer id){
        fromAnotherSource.add(id);
    }

    public void addIndividual(FileSong.Individual individual){
        individuals.put(individual.id(), individual.trackId());
    }

    private void checkStructure(){
        responses.forEach((_, statusMap)-> {
            for (Response.Status value : Response.Status.values()) {
                statusMap.putIfAbsent(value,new HashMap<>());
            }
        });
    }


}
