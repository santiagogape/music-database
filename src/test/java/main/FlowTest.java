package main;

import Main.MainDatabase;
import Main.MainRepository;

import app.control.Flow;
import app.control.TokenManager;
import app.control.api.*;
import app.control.api.batches.BatchSearchMultipleEndpoint;
import app.control.api.batches.TrackBatchSearcher;
import app.control.api.endpoints.MultipleEndpoint;
import app.control.files.ProcessMetadataToRequestString;
import app.control.files.actors.*;
import app.control.files.json.JsonProcessor;
import app.control.items.ItemIDUpdater;
import app.control.items.ResponseCreator;
import app.control.items.ResponseStatusUpdater;
import app.model.resources.FileSongStatusToAddedResponse;
import app.model.resources.FilesFromDirectory;
import app.model.utilities.api.*;
import app.model.utilities.api.factories.implementations.*;
import app.model.utilities.api.factories.implementations.EndpointSearchResultBasicWithImagesAndGenreLabeledFactory;
import dependencies.control.GsonJsonProcessor;
import app.control.files.actors.mp3Metadata.MP3Processor;

import app.model.items.*;
import app.model.repositories.FilesResponsesRepository;
import app.model.repositories.GenresRepository;
import app.model.repositories.ImagesRepository;
import app.model.repositories.ItemsRepository;
import app.model.utilities.database.Database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dependencies.control.JAudioTaggerMP3MetadataReader;
import dependencies.control.spotify.search.spotifyTrackBatchSearcher;
import dependencies.model.SQLite.MusicSQLiteDatabase;
import dependencies.control.spotify.SpotifyTokenManager;
import dependencies.model.resources.FileSongResponseTrackSearchResult;
import dependencies.model.resources.FileSongResponseTrackSearchResultList;
import dependencies.model.spotify.adapters.TrackSearchResultAdapter;
import dependencies.model.spotify.api.*;
import dependencies.model.spotify.api.factoryFillers.SpotifyMultipleAlbumsEndpointSearchResultWithImagesFactoryFiller;
import dependencies.model.spotify.api.factoryFillers.SpotifyMultipleArtistsEndpointSearchResultWithImagesAndGenreLabeledFactoryFiller;
import dependencies.model.spotify.auth.ClientCredentials;
import dependencies.model.spotify.auth.TokenRequest;
import dependencies.model.spotify.items.SpotifyMultipleArtists;
import dependencies.model.spotify.items.SpotifyMultipleAlbums;

import java.io.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDate;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.*;

public class FlowTest {

    //UTILITIES
    static final Scanner SCANNER = new Scanner(System.in);
    static final HttpClient CLIENT = HttpClient.newHttpClient();
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //DATABASE
    static final Path DB_FOLDER = Path.of("C:\\Users\\santi\\Desktop\\musica");
    static final String DB_NAME = "music.db";
    static final MainDatabase DATABASE = new MainDatabase(new MusicSQLiteDatabase(DB_FOLDER.toString(), DB_NAME));
    static final Path TEMP_FOLDER = existsOrCreateFolder("temp");
    static final Path RESPONSES_FOLDER = existsOrCreateFolder("responses");

    //Repositories
    static final MainRepository REPOSITORY = new MainRepository();
    public static final Path THIS_YEAR_FOLDER = DB_FOLDER.resolve("music " + LocalDate.now().getYear());

    //CONTROL ACTORS
    static final JsonProcessor JSON_PROCESSOR = new GsonJsonProcessor(GSON);
    static final MP3Processor MP3_PROCESSOR = new MP3Processor(new JAudioTaggerMP3MetadataReader(),new ProcessMetadataToRequestString());
    static final TempFolderReader TEMP_FOLDER_READER = new TempFolderReader(MP3_PROCESSOR,new FileSongFromTempFolderToThisYearsFolderMover(TEMP_FOLDER,THIS_YEAR_FOLDER));

    static final ResponseStatusUpdater RESPONSE_STATUS_UPDATER= new ResponseStatusUpdater();
    static final ItemIDUpdater ITEM_ID_UPDATER = new ItemIDUpdater();
    static final ResponseCreator RESPONSE_CREATOR = new ResponseCreator();

    //API
    static final ClientCredentials CLIENT_CREDENTIALS =
            fromJsonResource();
    static final TokenManager TOKEN_MANAGER =
            new SpotifyTokenManager(CLIENT, new TokenRequest(CLIENT_CREDENTIALS));

    static final EndpointRequest ENDPOINT_REQUEST = new SpotifyEndpointRequest(CLIENT);
    static final TrackSearchEndpoint TRACK_SEARCH_ENDPOINT = new SpotifyTrackSearchEndpoint(JSON_PROCESSOR,ENDPOINT_REQUEST);
    static final TrackBatchSearcher TRACK_BATCH_SEARCHER = new spotifyTrackBatchSearcher(TOKEN_MANAGER,20, TRACK_SEARCH_ENDPOINT);

    static final BatchSearchMultipleEndpoint<
            SpotifyMultipleArtists,
            Artist,
            EndpointMultipleSearchResultWithImagesAndGenreLabeled<Artist>,
            EndpointSearchResultBasicWithImagesAndGenreLabeledFactory<Artist>> ARTIST_BATCH_SEARCHER =
            new BatchSearchMultipleEndpoint<>(
                    TOKEN_MANAGER,
                    20,
                    new MultipleEndpoint<>(
                            JSON_PROCESSOR,
                            ENDPOINT_REQUEST,
                            50,
                            new EndpointSearchResultBasicWithImagesAndGenreLabeledFactory<>(),
                            new SpotifyMultipleArtistsEndpointSearchResultWithImagesAndGenreLabeledFactoryFiller(),
                            "https://api.spotify.com/v1/artists",
                            SpotifyMultipleArtists.class)
            );

    static final BatchSearchMultipleEndpoint<
            SpotifyMultipleAlbums,
            Album,
            EndpointMultipleSearchResultWithImages<Album>,
            EndpointSearchResultBasicWithImagesFactory<Album>> ALBUM_BATCH_SEARCHER =
            new BatchSearchMultipleEndpoint<>(TOKEN_MANAGER, 20, new MultipleEndpoint<>(
                    JSON_PROCESSOR,
                    ENDPOINT_REQUEST,
                    20,
                    new EndpointSearchResultBasicWithImagesFactory<>(),
                    new SpotifyMultipleAlbumsEndpointSearchResultWithImagesFactoryFiller(),
                    "https://api.spotify.com/v1/albums",
                    SpotifyMultipleAlbums.class
            ));

    // utility methods

    private static InputStreamReader getInputStreamReaderFromResources() {
        System.out.println("inputStream reader from "+ "/client-credentials.json");
        return new InputStreamReader(
                Objects.requireNonNull(
                        mainTest.class.getResourceAsStream("/client-credentials.json")
                ),
                StandardCharsets.UTF_8);
    }

    private static ClientCredentials fromJsonResource(){
        return GSON.fromJson(getInputStreamReaderFromResources(), ClientCredentials.class);
    }


    private static Path existsOrCreateFolder(String folderName) {
        Path resolve = DB_FOLDER.resolve(folderName);
        if (!Files.exists(resolve)){
            try {
                Path result = Files.createDirectory(resolve);
                System.out.println(result);

                return result;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(folderName+" exits");
        if (folderName.equals("temp") ||
                folderName.equals("responses") ) return resolve;
        System.out.println("not temp or responses");
        System.out.println(folderName.equals("music 2025"));
        System.out.println("with database?");
        System.out.println(DATABASE.getDirectoriesTable().get(folderName).isEmpty());
        if (DATABASE.getDirectoriesTable().get(folderName).isEmpty()) {
            System.out.println("adding to database");
            addNewFolder(folderName);
        }
        return resolve;
    }

    private static String readText(){
        return SCANNER.nextLine();
    }

    private static int readInt(){
        return SCANNER.nextInt();
    }


    /**
     * read database and fill repositories
     */
    private static void readDatabaseAndFillRepositories() {
        final Database.TableIntID<FileSong> filesTable = DATABASE.getFilesTable();
        final Database.UpdateTableIntID<Response> responsesTable = DATABASE.getResponsesTable();
        final Database.TableStringID<String> directoriesTable = DATABASE.getDirectoriesTable();
        final Database.TableIntID<Integer> sourcesTable = DATABASE.getSourcesTable();

        Database.TableIntID<ItemImage> imagesTable = DATABASE.getImagesTable();
        Database.TableIntID<ImageRef.ItemImageRef> webImagesTable = DATABASE.getWebImagesTable();


        final Database.TableIntID<Artist> artistsTable = DATABASE.getArtistsTable();
        final Database.TableIntID<Album> albumsTable = DATABASE.getAlbumsTable();
        final Database.TableIntID<Track> tracksTable = DATABASE.getTracksTable();
        final Database.TableIntID<SimpleItem.ItemUri> objectsTable = DATABASE.getObjectsTable();

        final Database.TableIntID<Album.AlbumArtist> albumArtistsTable = DATABASE.getAlbumArtists();
        final Database.TableIntID<Genre.ItemGenre> itemGenresTable = DATABASE.getItemGenres();
        final Database.TableIntID<Track.TrackArtist> trackArtistsTable = DATABASE.getTrackArtists();

        Database.Table<Genre> genresTable = DATABASE.getGenresTable();

        List<Genre> genres = genresTable.all();

        List<FileSong> files = filesTable.all();
        List<Response> responses = responsesTable.all();
        List<String> directories = directoriesTable.all();
        System.out.println(directories);
        List<Integer> sources = sourcesTable.all();

        List<ItemImage> localImages = imagesTable.all();
        List<ImageRef.ItemImageRef> webImages = webImagesTable.all();

        List<Artist> artists = artistsTable.all();
        List<Album> albums = albumsTable.all();
        List<Track> tracks = tracksTable.all();

        List<SimpleItem.ItemUri> items = objectsTable.all();
        List<Album.AlbumArtist> albumArtists = albumArtistsTable.all();
        List<Track.TrackArtist> trackArtists = trackArtistsTable.all();
        List<Genre.ItemGenre> itemGenres = itemGenresTable.all();

        Map<Integer, List<Integer>> artistAlbumsIds = getArtistAlbumsIds(albumArtists);

        Map<Integer, List<Integer>> albumTracksIds = getAlbumTracksIds(tracks);

        Map<Integer, List<Integer>> trackArtistsIds = getTrackArtistsIds(trackArtists);

        List<FileSong.Individual> individuals = DATABASE.getIndividualsTable().all();

        ItemsRepository itemsRepository = new ItemsRepository(
                artists.stream().collect(Collectors.toMap(SimpleItem::id, a -> a)),
                albums.stream().collect(Collectors.toMap(SimpleItem::id, a -> a)),
                tracks.stream().collect(Collectors.toMap(SimpleItem::id, a -> a)),
                artistAlbumsIds,
                albumTracksIds,
                trackArtistsIds,
                items.stream().collect(
                        Collectors.groupingBy(SimpleItem.ItemUri::source,
                                Collectors.groupingBy(
                                        SimpleItem.ItemUri::type,
                                        toMap(SimpleItem.ItemUri::sourceId,i->i)
                                ))));
        FilesResponsesRepository filesResponsesRepository = FilesResponsesRepository.from(files,responses,new HashSet<>(sources), individuals);

        Map<Integer, List<Genre>> itemGenresIds = getItemGenresIds(itemGenres);

        GenresRepository genresRepository = new GenresRepository(
                getItemGenresFromItemUriAndSourceIdWithGenres(itemGenresIds, artists),
                getItemGenresFromItemUriAndSourceIdWithGenres(itemGenresIds, albums),
                getItemGenresFromItemUriAndSourceIdWithGenres(itemGenresIds, tracks),
                genres
        );

        REPOSITORY.setDirectories(directories);
        REPOSITORY.setGenresRepository(genresRepository);
        REPOSITORY.setItemsRepository(itemsRepository);
        REPOSITORY.setFilesResponsesRepository(filesResponsesRepository);
        REPOSITORY.setImagesRepository(new ImagesRepository(localImages,webImages));

        System.out.println("creating if no exists");
        existsOrCreateFolder(THIS_YEAR_FOLDER.getFileName().toString());
    }

    private static <T extends SimpleItem>  Map<Integer, List<Genre>> getItemGenresFromItemUriAndSourceIdWithGenres(Map<Integer, List<Genre>> itemGenresIds, List<T> items) {
        return itemGenresIds.entrySet()
                .stream()
                .filter(i -> items.stream().anyMatch(a -> Objects.equals(a.id(), i.getKey())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private static Map<Integer, List<Genre>> getItemGenresIds(List<Genre.ItemGenre> itemGenres) {
        return itemGenres.stream().collect(Collectors.groupingBy(Genre.ItemGenre::item))
                .entrySet().stream()
                .collect(toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().map(Genre.ItemGenre::genre).toList()));
    }

    private static Map<Integer, List<Integer>> getTrackArtistsIds(List<Track.TrackArtist> trackArtists) {
        return trackArtists.stream().collect(Collectors.groupingBy(Track.TrackArtist::trackId)).entrySet()
                .stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Track.TrackArtist::artistsId).toList()));
    }

    private static Map<Integer, List<Integer>> getAlbumTracksIds(List<Track> tracks) {
        return tracks.stream().collect(Collectors.groupingBy(Track::albumId))
                .entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().stream().map(SimpleItem::id).collect(Collectors.toList())));
    }

    private static Map<Integer, List<Integer>> getArtistAlbumsIds(List<Album.AlbumArtist> albumArtists) {
        return albumArtists.stream()
                .collect(Collectors.groupingBy(Album.AlbumArtist::artistsId)).entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Album.AlbumArtist::albumId).collect(Collectors.toList())));
    }
    /**
     *  add to database and repositories
     *  todo -> modify repositories, make sure are modifiable
     */

    private static List<FileSong> addFileSongs(List<FileSong> files) {
        return files.stream()
                .map(f -> DATABASE.getFilesTable().insert(f))
                .peek(f->REPOSITORY.getFilesResponsesRepository().addFile(f.directory(),f))
                .toList();
    }

    private static void updateResponseFromNotChecked(FileSong directory, Response response){
        DATABASE.getResponsesTable().update(response);
        REPOSITORY.getFilesResponsesRepository().updateResponse(directory, Response.Status.not_checked,response);
    }

    private static void updateResponsesToAdded(List<FileSongStatusToAddedResponse> responses){
        responses.forEach(r->{
            DATABASE.getResponsesTable().update(r.updated());
            REPOSITORY.getFilesResponsesRepository().updateResponse(r.fileSong(),r.previous(),r.updated());
        });
    }

    private static void addNewFolder(String thisYear) {
        DATABASE.getDirectoriesTable().insert(thisYear);
        REPOSITORY.getDirectories().add(thisYear);
    }

    private static void addResponseToDatabaseRepositoryResponsesFolder(FileSongResponseTrackSearchResultList result) {
        JSON_PROCESSOR.write(result.result(),RESPONSES_FOLDER.resolve(result.response().nameWithExtension()));
        DATABASE.getResponsesTable().insert(result.response());
        REPOSITORY.getFilesResponsesRepository().addResponse(result.fileSong(),result.response());
    }

    private static List<SimpleItem.ItemUri> addItems(List<SimpleItem.ItemUri> items) {
        return items.stream().map(i->DATABASE.getObjectsTable().insert(i))
                .peek(i->REPOSITORY.getItemsRepository().addItemUri(i)).toList();
    }

    private static void addArtists(List<Artist> artists) {
        artists.stream()
                .map(a -> DATABASE.getArtistsTable().insert(a))
                .forEach(i->REPOSITORY.getItemsRepository().addArtist(i));
    }

    private static void addAlbum(List<Album> albums) {
        albums.stream().map(a -> DATABASE.getAlbumsTable().insert(a))
                .forEach(a->REPOSITORY.getItemsRepository().addAlbum(a));

    }

    private static void addAlbumArtists(List<Album.AlbumArtist> list) {
        list.stream()
                .peek(aa-> System.out.println("inserting "+aa))
                .map(aa -> DATABASE.getAlbumArtists().insert(aa))
                .peek(aa-> System.out.println("inserted "+aa))
                .forEach(aa->REPOSITORY.getItemsRepository().addArtistsAlbum(aa.artistsId(),aa.albumId()));
    }

    private static void addArtistsGenres(List<Genre.ItemGenre> list){
        list.stream().peek(ag-> DATABASE.getItemGenres().insert(ag)).forEach(
                ag->REPOSITORY.getGenresRepository().addArtistGenres(ag.item(),ag.genre())
        );
    }

    private static void addGenre(String g) {
        REPOSITORY.getGenresRepository().addGenre(DATABASE.getGenresTable().insert(new Genre(g)));
    }

    private static void addTracks(
            List<FileSongResponseTrackSearchResult> results,
            Map<String,Artist> artistsBySourceId,
            Map<String, Album> albumsBySourceId) {
        List<SimpleItem.ItemUri> items = new ArrayList<>();
        Map<String, Track> tracksBySourceId = new HashMap<>();
        Map<String, Set<Genre>> trackGenresBySourceId = new HashMap<>();
        Map<String,List<Artist>> trackArtistsBySourceId = new HashMap<>();

        System.out.println("using");
        System.out.println(artistsBySourceId);
        System.out.println(albumsBySourceId);
        System.out.println("filling");
        fillListAndMapsOfTrackWithSpotifyTracksAndAlbumsAndArtists(results, artistsBySourceId,albumsBySourceId , items, tracksBySourceId, trackGenresBySourceId, trackArtistsBySourceId);

        List<Genre.ItemGenre> trackGenres = new ArrayList<>();
        List<Track.TrackArtist> trackArtists = new ArrayList<>();

        List<SimpleItem.ItemUri> itemsWithId = addItems(items);
        System.out.println("\"artistsBySource\""+ GSON.toJson(trackArtistsBySourceId));
        List<Track> tracks = itemsWithId.stream()
                .peek(i->trackGenresBySourceId.get(i.sourceId()).forEach(g->trackGenres.add(new Genre.ItemGenre(i.id(),g))))
                .peek(i-> System.out.println(trackArtistsBySourceId.containsKey(i.sourceId())))
                .peek(i->trackArtistsBySourceId.get(i.sourceId()).forEach(a->trackArtists.add(new Track.TrackArtist(i.id(),a.id()))))
                .map(i -> ITEM_ID_UPDATER.update(i.id(), tracksBySourceId.get(i.sourceId()))).toList();
        System.out.println("\"genres\""+ GSON.toJson(trackGenres));
        System.out.println("\"artists\""+ GSON.toJson(trackArtists));
        System.out.println("adding tracks to database");
        addTracks(tracks, trackGenres, trackArtists);

        List<FileSongStatusToAddedResponse> fileSongStatusToAddedResponses = results.stream().map(r ->
                new FileSongStatusToAddedResponse(
                        r.fileSong(),
                        r.response().status(),
                        RESPONSE_STATUS_UPDATER.updateStatus(r.response(), Response.Status.added))).toList();
        updateResponsesToAdded(fileSongStatusToAddedResponses);
    }


    private static void addTracks(List<Track> list, List<Genre.ItemGenre> trackGenres, List<Track.TrackArtist> trackArtists) {
        System.out.println("inserting");
        list.forEach(t-> DATABASE.getTracksTable().insert(t));
        System.out.println("tracks");
        trackGenres.forEach(tg-> DATABASE.getItemGenres().insert(tg));
        System.out.println("genres");
        trackArtists.forEach(ta-> DATABASE.getTrackArtists().insert(ta));
        System.out.println("in repository");
        list.forEach(t->REPOSITORY.getItemsRepository().addTrack(t));
        list.forEach(t->REPOSITORY.getItemsRepository().addAlbumTrack(t.albumId(),t.id()));
        trackGenres.forEach(tg->REPOSITORY.getGenresRepository().addTrackGenres(tg.item(),tg.genre()));
        trackArtists.forEach(ta-> REPOSITORY.getItemsRepository().addTrackArtist(ta.trackId(),ta.artistsId()));
    }

    private static void addItemWebImages(List<ImageRef.ItemImageRef> images) {
        System.out.println("adding images");
        images.forEach(i->{
            System.out.println(i+ " inserting");
            DATABASE.getWebImagesTable().insert(i);
            System.out.println(i+"inserted in database");
            REPOSITORY.getImagesRepository().addWebImage(i);
            System.out.println("inserted in repo");
        });
    }

    /**
     * FLOW /////////////////////// /////////////////////// /////////////////////// ///////////////////////
     */

    //TODO -> Control of flow for when there's not new fileSongs to be searched
    public static void main(String[] args) {
        System.out.println("waiting 10 seconds for initialization");
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("reading database and filling repositories");
        readDatabaseAndFillRepositories();
        System.out.println("database and repositories ready");
        FilesFromDirectory read = read();
        TOKEN_MANAGER.start();
        System.out.println(read.files());
        if (read.areNew()){
            List<FileSong> fileSongs = addFileSongs(read.files());
            requestFileSongs(fileSongs); // jump to selection(List<FileSongResponseSpotifyTrackSearchResponse> requestedData)
            System.out.println("returned from requested");
        } else {
            String directory = read.directory();
            boolean somethingIsProcessing = ChooseFlowFromDatabase(directory);
            while (!somethingIsProcessing){
                System.out.println("want to check another filter? yes/no");
                if (readText().equals("yes")) somethingIsProcessing = ChooseFlowFromDatabase(directory);
                System.out.println("wan to check another directory in the database? yes/no");
                if (readText().equals("yes")) {
                    somethingIsProcessing = ChooseFlowFromDatabase(readFromDatabase().directory());
                }
                somethingIsProcessing = true;
            }
        }

        System.out.println("ending token manager?");
        DATABASE.close();
        TOKEN_MANAGER.end();
    }

    private static boolean ChooseFlowFromDatabase(String directory) {
        Flow actionFlow = chooseFilter();
        FilesResponsesRepository filteredByDirectory = REPOSITORY.getFilesResponsesRepository().filterByDirectory(directory);
        switch (actionFlow) {
            case NO_RESPONSE -> {
                List<FileSong> fileSongs = filteredByDirectory.filesWithoutResponse();
                if (fileSongs.isEmpty()) {
                    System.out.println("there's no files to be requested in this directory, returning");
                    return false;
                }
                requestFileSongs(fileSongs);
                System.out.println("returned from requested");
                return true;
            }

            case NOT_CHECKED -> {
                List<FileSongResponseTrackSearchResultList> list = filteredByDirectory.filterByResponseStatus(Response.Status.not_checked).toMap().entrySet().stream()
                        .map(e -> new FileSongResponseTrackSearchResultList(
                                        e.getKey(),
                                        e.getValue(),
                                        readResponseOf(e.getValue())
                                )
                        ).toList();
                if (list.isEmpty()) {
                    System.out.println("there's no files to be checked in this directory, returning");
                    return false;
                }
                selection(list);
                return true;
            }
            case NOT_CONTAINED -> {
                FilesResponsesRepository filesResponsesRepository = filteredByDirectory.filterByResponseStatus(Response.Status.checked_not_contained);
                Map<FileSong, Response> map = filesResponsesRepository.toMap();
                if (map.isEmpty()) {
                    System.out.println("there's no files to be rechecked (not contained) in this directory, returning");
                    return false;
                }
                notContainedFlow(map); //todo...
                return true;
            }

            case CONTAINED_NOT_ADDED -> {
                FilesResponsesRepository filesResponsesRepository = filteredByDirectory.filterByResponseStatus(Response.Status.checked_contained);
                Set<String> albumsId = new HashSet<>();
                Set<String> artistsId = new HashSet<>();
                Map<String, Artist> existingArtistsBySourceId = new HashMap<>();
                Map<String, Album> existingAlbumsBySourceId = new HashMap<>();


                List<FileSongResponseTrackSearchResult> list =
                        filesResponsesRepository.toMap().entrySet().stream()
                            .peek(entry-> System.out.println(DB_FOLDER.resolve(entry.getValue().directory()).resolve(entry.getValue().nameWithExtension())))
                            .map(entry ->
                                    new FileSongResponseTrackSearchResult(
                                        entry.getKey(),
                                        entry.getValue(),
                                        JSON_PROCESSOR.read(
                                                TrackSearchResultList.TrackSearchResult.class,
                                                DB_FOLDER.resolve(entry.getValue().directory()).resolve(entry.getValue().nameWithExtension()))
                                    ))
                            .peek(t-> System.out.println(t.trackSearchResult().trackName()))
                            .toList();
                if (list.isEmpty()){
                    System.out.println("there's no files to be added in this directory, returning");
                    return false;
                }
                extractArtistsAndAlbumsFromFileSongResponseTrackSearchResult(
                        list,
                        albumsId,
                        artistsId,
                        existingArtistsBySourceId,
                        existingAlbumsBySourceId);

                System.out.println(existingArtistsBySourceId);
                System.out.println(artistsId);
                System.out.println(existingAlbumsBySourceId);
                System.out.println(albumsId);
                startRequestForArtistsAndAlbums(
                        albumsId,
                        list,
                        artistsId,
                        existingArtistsBySourceId,
                        existingAlbumsBySourceId);
                return true;
            }
        }
        System.err.println("it shouldn't be here");
        return false;
    }

    private static TrackSearchResultList readResponseOf(Response value) {
        return JSON_PROCESSOR.read(TrackSearchResultList.class,RESPONSES_FOLDER.resolve(value.nameWithExtension()));
    }

    /**
     * 1. GET FILE-SONGS /////////////////////// /////////////////////// ///////////////////////
     */
    private static FilesFromDirectory read() {
        System.out.println("read from (files, database):");
        String s = readText();
        if (Objects.equals(s, "files")){
            return readFromFiles();
        } else if (Objects.equals(s, "database")){
            return readFromDatabase();
        } else {
            System.out.println("wrong");
            return read();
        }
    }

    private static FilesFromDirectory readFromFiles() {
        System.out.println("read from (temp, folder):");
        if (Objects.equals(readText(), "temp")){
            return TEMP_FOLDER_READER.readTemp();
        } else if (Objects.equals(readText(), "folder")){
            return readFromFolder();
        } else {
            System.out.println("wrong");
            return readFromFiles();
        }
    }

    private static FilesFromDirectory readFromFolder() {
        //todo -> this wil be converted to an window of a directoryChooser, filtering the temp an responses folder
        try(Stream<Path> directories = Files.list(DB_FOLDER).filter(Files::isDirectory).filter(p->p!= TEMP_FOLDER && p!= RESPONSES_FOLDER)) {
            List<Path> list = directories.toList();
            System.out.println("read from:");
            list.forEach(System.out::println);
            System.out.println("choose:");
            String input = readText();
            if (list.contains(DB_FOLDER.resolve(input))) {
                if (REPOSITORY.getDirectories().contains(input)) return readFromDirectory(input);
                return new FilesFromDirectory(input,true, MP3_PROCESSOR.process(DB_FOLDER.resolve(input)));
            } else {
                return readFromFolder();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FilesFromDirectory readFromDatabase() {
        System.out.println("read from one of this directories:");
        REPOSITORY.getDirectories().forEach(System.out::println);
        String input = readText();
        if (REPOSITORY.getDirectories().contains(input)) return readFromDirectory(input);
        else return readFromDatabase();
    }

    private static FilesFromDirectory readFromDirectory(String input) {
        return new FilesFromDirectory(input,false,List.of());
    }


    private static Flow chooseFilter() {

        System.out.println("choose a filter by number");
        for (Flow value : Flow.values()) {
            System.out.println(value.description());
        }
        int i = readInt();
        for (Flow value: Flow.values()){
            if (value.ordinal()==i) return value;
        }
        System.out.println("not the correct range");
        return chooseFilter();
    }

    /**
     * 2.1 REQUEST FILE-SONGS /////////////////////// /////////////////////// ///////////////////////
     */
    private static void requestFileSongs(List<FileSong> fileSongs) {
        List<String> queries = fileSongs.stream().map(FileSong::request).toList();
        List<FileSongResponseTrackSearchResultList> results = new ArrayList<>();
        TRACK_BATCH_SEARCHER.startNewBatchWith(queries,createResponseObserverOfTrackSearchResultList(fileSongs, results));
        System.out.println("started track selection, returning");
        selection(results);
    }

    private static ResponseObserver<TrackSearchResultList> createResponseObserverOfTrackSearchResultList(List<FileSong> fileSongs, List<FileSongResponseTrackSearchResultList> results) {
        return new ResponseObserver<>() {
            @Override
            public void notify(TrackSearchResultList result) {
                fileSongs.stream().filter(f->f.request().equals(result.query())).findFirst().ifPresent((f)->{
                    FileSongResponseTrackSearchResultList record = new FileSongResponseTrackSearchResultList(f, RESPONSE_CREATOR.createFrom(f), result);
                    addResponseToDatabaseRepositoryResponsesFolder(record);
                    results.add(record);
                });
            }

            @Override
            public void finish(CountDownLatch finisher) {
                finisher.countDown();
            }
        };
    }


    /**
     * 3. SELECTION /////////////////////// /////////////////////// ///////////////////////
     *
     */

    private static void selection(List<FileSongResponseTrackSearchResultList> requestedData){
        List<FileSongResponseTrackSearchResult> results = new ArrayList<>();
        requestedData.forEach(data -> {
            if (data.result().tracks().isEmpty()){
                markAsCheckedNotContained(data);
            } else {
                // table view:
                System.out.println("\n\n///////////////// Table ///////////////////");
                System.out.println("entries for:"+ data.fileSong().name());
                cellViewFor(data.result());
                Optional<TrackSearchResultList.TrackSearchResult> selected =  selectTrack(data.result());
                selected.ifPresentOrElse(
                        trackSearchResult-> {
                            results.add(new FileSongResponseTrackSearchResult(data.fileSong(), data.response(), trackSearchResult));
                            markAsCheckedAndContained(data);
                            rewriteJson(data.response(), trackSearchResult);
                        },
                        () -> markAsCheckedNotContained(data)
                        );
            }
        });

        // no doppelgangers
        Set<String> albumsId = new HashSet<>();
        Set<String> artistsId = new HashSet<>();
        Map<String, Artist> existingArtistBySourceId = new HashMap<>();
        Map<String, Album> existingAlbumsBySourceID = new HashMap<>();
        
        extractArtistsAndAlbumsFromFileSongResponseTrackSearchResult(results, albumsId, artistsId, existingArtistBySourceId, existingAlbumsBySourceID);

        // first request artists, then albums, so the observers need access to resources in the opposite order
        // when artist batch search is finished, it will call another function which will start the batch album search
        // when the batch album search is finished, it will call another function to add the tracks to the database,
        // given that both, artists and album will exist in the database

        startRequestForArtistsAndAlbums(
                albumsId,
                results,
                artistsId,
                existingArtistBySourceId,
                existingAlbumsBySourceID);
    }

    private static void extractArtistsAndAlbumsFromFileSongResponseTrackSearchResult(
            List<FileSongResponseTrackSearchResult> results,
            Set<String> albumsId,
            Set<String> artistsId,
            Map<String, Artist> ArtistsBySourceId,
            Map<String, Album> AlbumsBySourceId) {
        results.forEach(r->{
            
            REPOSITORY.getItemsRepository()
                    .getAlbumUriByItem(Database.ItemSource.spotify,r.trackSearchResult().albumId())
                    .ifPresentOrElse(
                            a->{
                                System.out.println("album:"+a.id()+"a already in database");
                                AlbumsBySourceId.put(r.trackSearchResult().albumId(), a);
                                r.trackSearchResult().albumArtistsIds().forEach(id-> REPOSITORY.getItemsRepository()
                                        .getArtistUriByItem(Database.ItemSource.spotify,id)
                                        .ifPresent(artist ->
                                                ArtistsBySourceId
                                                        .putIfAbsent(id, artist) ));
                            },
                            ()->{
                                albumsId.add(r.trackSearchResult().albumId());
                                checkArtistsAndAddIfNotInDatabase(artistsId, ArtistsBySourceId, r.trackSearchResult().albumArtistsIds());
                            }
                            );
            checkArtistsAndAddIfNotInDatabase(artistsId, ArtistsBySourceId, r.trackSearchResult().artistsId());
        });
    }

    private static void checkArtistsAndAddIfNotInDatabase(Set<String> artistsId, Map<String, Artist> ArtistsBySourceId, List<String> artistsToCheck) {
        artistsToCheck.stream()
                .map(a-> Map.entry(a,REPOSITORY.getItemsRepository().getArtistUriByItem(Database.ItemSource.spotify, a)))
                .forEach(e-> e.getValue().ifPresentOrElse(
                        artist-> {
                            System.out.println("artist:"+artist.id()+" already in database");
                            ArtistsBySourceId
                                    .putIfAbsent(e.getKey(), artist);
                        } ,
                        ()->{
                            System.out.println("artist not in database, adding to search:"+e.getKey());
                            artistsId.add(e.getKey());
                        }
                ));
    }

    private static void rewriteJson(Response response, TrackSearchResultList.TrackSearchResult TrackSearchResult) {
        JSON_PROCESSOR.write(TrackSearchResult,RESPONSES_FOLDER.resolve(response.nameWithExtension()));
    }

    private static void markAsCheckedAndContained(FileSongResponseTrackSearchResultList data) {
        Response response = RESPONSE_STATUS_UPDATER.updateStatus(data.response(), Response.Status.checked_contained);
        updateResponseFromNotChecked(data.fileSong(), response);
    }

    private static Optional<TrackSearchResultList.TrackSearchResult> selectTrack(TrackSearchResultList result) {
        System.out.println("chose track by id or mark as 'not contained':");
        String s = readText();
        if (result.tracks().stream().anyMatch(t-> t.trackId().equals(s))) {
            return result.tracks().stream().filter(t-> t.trackId().equals(s)).findFirst();
        } else if (s.equals("not contained")) {
            return Optional.empty();
        } else {
            System.out.println("wrong input. write a track id or 'not contained' without '' ");
            return selectTrack(result);
        }
    }

    private static void cellViewFor(TrackSearchResultList result) {
        result.tracks().forEach(i -> {
            System.out.println("////////////////////////////////////");
            System.out.println("id: "+i.trackId());
            System.out.println("name: "+i.trackName());
            System.out.println("spotify: "+i.trackUrl());
            System.out.println("album id: "+i.albumId());
            System.out.println("album spotify: "+i.albumUrl());
            System.out.println("first artist id: "+i.artistsId().getFirst());
            System.out.println("first artist spotify: "+i.artistsUrl().getFirst());
        });
    }

    private static void markAsCheckedNotContained(FileSongResponseTrackSearchResultList data) {
        Response response = RESPONSE_STATUS_UPDATER.updateStatus(data.response(), Response.Status.checked_not_contained);
        updateResponseFromNotChecked(data.fileSong(), response);
    }



    /**
     * 4. SEARCH ARTISTS AND ALBUMS, THEN ADD TRACK
     */

    private static void startRequestForArtistsAndAlbums(
            Set<String> albumsId,
            List<FileSongResponseTrackSearchResult> results,
            Set<String> artistsId,
            Map<String, Artist> existingArtistsBySourceId,
            Map<String, Album> existingAlbumsBySourceId
    ) {

        if (!artistsId.isEmpty()) {
            System.out.println("searching artists:"+artistsId);
            ARTIST_BATCH_SEARCHER.startNewBatchWith(List.copyOf(artistsId),
                    createResponseObserverForArtistsBatchSearchWithMultipleEndpoint(existingArtistsBySourceId));
        }

        if (!albumsId.isEmpty()) {
            System.out.println("searching albums+:"+albumsId);
            ALBUM_BATCH_SEARCHER.startNewBatchWith(List.copyOf(albumsId),
                    crateResponseObserverForAlbumsBatchSearchWithMultipleEndpoint(
                            existingAlbumsBySourceId, existingArtistsBySourceId, results));
        }

        System.out.println("sending:");
        System.out.println(existingArtistsBySourceId);
        System.out.println(existingAlbumsBySourceId);
        addTracks(results,existingArtistsBySourceId,existingAlbumsBySourceId);

    }

    private static ResponseObserver<EndpointMultipleSearchResultWithImages<Album>>
    crateResponseObserverForAlbumsBatchSearchWithMultipleEndpoint(
            Map<String, Album> albumsBySourceId, Map<String, Artist> artistsBySourceId, List<FileSongResponseTrackSearchResult> results) {
        return new ResponseObserver<>() {
            @Override
            public void notify(EndpointMultipleSearchResultWithImages<Album> result) {
                System.out.println("albums notified");
                List<SimpleItem.ItemUri> itemsWithId = addItems(result.itemsUris());
                System.out.println("added items");
                List<Album> list = itemsWithId
                        .stream()
                        .map(i -> ITEM_ID_UPDATER.update(i.id(), result.items().get(i.sourceId())))
                        .toList();
                addAlbum(list);
                System.out.println("added albums");
                for (int i = 0; i < itemsWithId.size(); i++) {
                    System.out.println(itemsWithId.get(i).sourceId()+":"+list.get(i).id());
                    albumsBySourceId.put(itemsWithId.get(i).sourceId(),list.get(i));
                }
                List<Album.AlbumArtist> albumArtists = results.stream().map(FileSongResponseTrackSearchResult::trackSearchResult)
                        .peek(r-> r.albumArtistsIds().forEach(a->{
                            System.out.println("album:"+r.albumId()+" artist:"+a+":"+artistsBySourceId.get(a).id());
                        }))
                        .map(r -> Map.entry(r.albumId(), r.albumArtistsIds()))
                        .flatMap(e ->
                                e.getValue().stream()
                                        .peek(artistId -> System.out.println("artist"+":"+artistId+":"+artistsBySourceId.get(artistId).id()))
                                        .map(
                                        artistId -> new Album.AlbumArtist(albumsBySourceId.get(e.getKey()).id(), artistsBySourceId.get(artistId).id()))
                        ).toList();
                System.out.println(albumArtists);
                addAlbumArtists(albumArtists);
                System.out.println("added relations");
                List<ImageRef.ItemImageRef> itemImagesFromItemUriAndSourceWithId = getItemImagesFromItemUriAndSourceWithId(itemsWithId, result.images());
                System.out.println(JSON_PROCESSOR.toJson(itemImagesFromItemUriAndSourceWithId));
                addItemWebImages(itemImagesFromItemUriAndSourceWithId);
                System.out.println("added  images");
            }

            @Override
            public void finish(CountDownLatch finisher) {
                finisher.countDown();
            }
        };
    }

    private static ResponseObserver<EndpointMultipleSearchResultWithImagesAndGenreLabeled<Artist>> createResponseObserverForArtistsBatchSearchWithMultipleEndpoint(Map<String, Artist> artistsBySourceId) {
        return new ResponseObserver<>() {

            @Override
            public void notify(EndpointMultipleSearchResultWithImagesAndGenreLabeled<Artist> result) {
                List<SimpleItem.ItemUri> itemsWithId = addItems(result.itemsUris());
                List<Artist> list = itemsWithId
                        .stream()
                        .map(i -> ITEM_ID_UPDATER.update(i.id(), result.items().get(i.sourceId())))
                        .toList();
                addArtists(list);
                for (int i = 0; i < itemsWithId.size(); i++) {
                    artistsBySourceId.put(itemsWithId.get(i).sourceId(),list.get(i));
                }

                result.genres().stream().filter(g -> !REPOSITORY.getGenresRepository().containsGenre(g)).forEach(g -> addGenre(g.name()));
                List<Genre.ItemGenre> itemGenresFromItemUriAndSourceIdWithGenres =
                        getItemGenresFromItemUriAndSourceIdWithGenres(itemsWithId,  result.itemGenres());
                System.out.println("obtained genres");
                addArtistsGenres(itemGenresFromItemUriAndSourceIdWithGenres);
                System.out.println("added genres ");
                addItemWebImages(getItemImagesFromItemUriAndSourceWithId(itemsWithId, result.images()));
                System.out.println("added  images");
            }

            @Override
            public void finish(CountDownLatch finisher) {
                finisher.countDown();
            }
        };
    }

    private static List<ImageRef.ItemImageRef> getItemImagesFromItemUriAndSourceWithId(List<SimpleItem.ItemUri> itemsWithId, Map<String, List<ImageRef>> imagesRefs) {
        return itemsWithId.stream().filter(i->imagesRefs.containsKey(i.sourceId()))
                .flatMap(i->imagesRefs.get(i.sourceId()).stream().map(ref->new ImageRef.ItemImageRef(i.id(),ref.url(),ref.height(),ref.width()))).toList();
    }


    private static void fillListAndMapsOfTrackWithSpotifyTracksAndAlbumsAndArtists(List<FileSongResponseTrackSearchResult> results,
                                                                                   Map<String, Artist> artistsBySourceId, Map<String, Album> albumsBySourceId,
                                                                                   List<SimpleItem.ItemUri> items,
                                                                                   Map<String, Track> tracksBySourceId,
                                                                                   Map<String, Set<Genre>> trackGenresBySourceId,
                                                                                   Map<String, List<Artist>> trackArtistsBySourceId) {
        System.out.println(artistsBySourceId);
        System.out.println(albumsBySourceId);

        results.forEach(
                fileSongResponseTrackSearchResult -> {
                    List<Artist> trackArtists = fileSongResponseTrackSearchResult.trackSearchResult()
                            .artistsId().stream()
                            .peek(ai-> System.out.println("artist:"+ai+":"+artistsBySourceId.get(ai)))
                            .map(artistsBySourceId::get).peek(a-> {
                                if (a == null) {
                                    System.err.println("an artist was null");
                                }
                            }).toList();
                    Album trackAlbum = albumsBySourceId
                            .get(fileSongResponseTrackSearchResult.trackSearchResult().albumId());
                    if (trackAlbum == null) {
                        System.err.println("the album is not in the map");
                    }

                    Set<Genre> trackGenres = trackArtists.stream().flatMap(a-> REPOSITORY.getGenresRepository().artistGenres(a).stream()).collect(toSet());
                    Track track = new TrackSearchResultAdapter(fileSongResponseTrackSearchResult.trackSearchResult(), fileSongResponseTrackSearchResult.fileSong(), trackAlbum);

                    items.add(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.track, fileSongResponseTrackSearchResult.trackSearchResult().trackId()));
                    tracksBySourceId.put(fileSongResponseTrackSearchResult.trackSearchResult().trackId(),track);
                    trackGenresBySourceId.put(fileSongResponseTrackSearchResult.trackSearchResult().trackId(),trackGenres);
                    trackArtistsBySourceId.put(fileSongResponseTrackSearchResult.trackSearchResult().trackId(), trackArtists);

                }
        );
    }


    private static List<Genre.ItemGenre> getItemGenresFromItemUriAndSourceIdWithGenres(List<SimpleItem.ItemUri> itemsWithId, Map<String, List<Genre>> fromSource) {
        System.out.println("streaming:"+itemsWithId+"\n"+fromSource);
        List<Genre.ItemGenre> list = itemsWithId.stream()
                .peek(System.out::println)
                .filter(i-> fromSource.containsKey(i.sourceId()))
                .peek(i -> System.out.println(Map.entry(i.id(), fromSource.get(i.sourceId()))))
                .map(i -> Map.entry(i.id(), fromSource.get(i.sourceId())))
                .peek(System.out::println)
                .flatMap(e -> e.getValue().stream().map(g -> new Genre.ItemGenre(e.getKey(), g)))
                .peek(System.out::println)
                .toList();
        System.out.println("as list");
        return list;
    }


    /**
     * 5. NOT CONTAINED
     */

    private static void notContainedFlow(Map<FileSong, Response> map) {
        /*  -´`'P.todo -> incoherence with current methods for normal flow,
             can't spread the "fromAnotherSource" once start the artist/album search
             */

        List<FileSong.FileSongResponseIndividualTrackId> individuals = new ArrayList<>();

        map.forEach((fileSong,response)->{
            Optional<String> trackId = askForATrackIdOrMarkFromOtherSource();
            trackId.ifPresentOrElse(
                    (stringId)-> {
                        individuals.add(new FileSong.FileSongResponseIndividualTrackId(fileSong, response, stringId));
                        REPOSITORY
                                .getFilesResponsesRepository()
                                .addIndividual(
                                        DATABASE
                                                .getIndividualsTable()
                                                .insert(new FileSong.Individual(fileSong.id(),stringId))
                                );
                    },
                    ()-> REPOSITORY
                            .getFilesResponsesRepository()
                            .addFromAnotherSource(
                                    DATABASE
                                            .getSourcesTable()
                                            .insert(fileSong.id())
                            ));
        });

        System.out.println("processing individual tracks");


    }


    private static Optional<String> askForATrackIdOrMarkFromOtherSource() {
        System.out.println("enter a track ID from the API or mark as 'other' if it's not in the api data:");
        String s = readText();
        if (s.equals("other")) return Optional.empty();
        return Optional.of(s);
    }

}

