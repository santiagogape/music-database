package main;

import Main.MainDatabase;
import Main.MainRepository;

import app.control.Flow;
import app.control.TokenManager;
import app.control.api.*;
import app.control.files.ProcessMetadataToRequestString;
import app.control.files.actors.*;
import app.control.items.ItemIDUpdater;
import app.control.items.ResponseCreator;
import app.control.items.ResponseStatusUpdater;
import app.model.resources.FileSongStatusToAddedResponse;
import app.model.resources.FilesFromDirectory;
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
import dependencies.model.SQLite.MusicSQLiteDatabase;
import dependencies.control.spotify.SpotifyTokenManager;
import dependencies.control.spotify.search.SpotifyMultipleAlbumSearch;
import dependencies.control.spotify.search.SpotifyMultipleArtistSearch;
import dependencies.control.spotify.search.SpotifyTrackSearch;
import dependencies.model.resources.ArtistsAndAlbumsResultListener;
import dependencies.model.resources.FileSongResponseSpotifyTrack;
import dependencies.model.resources.FileSongResponseSpotifyTrackSearchResponse;
import dependencies.model.spotify.adapters.SpotifyAlbumAdapter;
import dependencies.model.spotify.adapters.SpotifyArtistAdapter;
import dependencies.model.spotify.adapters.SpotifyTrackAdapter;
import dependencies.model.spotify.auth.ClientCredentials;
import dependencies.model.spotify.auth.TokenRequest;
import dependencies.model.spotify.items.SpotifyMultipleArtists;
import dependencies.model.spotify.items.SpotifyTrackSearchResponse;
import dependencies.model.spotify.items.full.SpotifyTrack;
import dependencies.model.spotify.items.simplified.SpotifyMultipleAlbums;
import dependencies.model.spotify.items.simplified.SpotifySimplifiedObject;

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
    static final CountDownLatch COUNT_DOWN_LATCH_FOR_FINISHED_FLOWS = new CountDownLatch(1);


    //API
    static final ClientCredentials CLIENT_CREDENTIALS =
            fromJsonResource("/client-credentials.json", ClientCredentials.class);
    static final TokenManager TOKEN_MANAGER =
            new SpotifyTokenManager(CLIENT, new TokenRequest(CLIENT_CREDENTIALS));
    static final Search TRACK_SEARCH = new SpotifyTrackSearch(CLIENT);
    public static final MultipleSearcher MULTIPLE_ARTIST_SEARCH = new SpotifyMultipleArtistSearch(CLIENT);
    public static final MultipleSearcher MULTIPLE_ALBUM_SEARCH = new SpotifyMultipleAlbumSearch(CLIENT);

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


    // utility methods

    private static InputStreamReader getInputStreamReaderFromResources(String path) {
        System.out.println("inputStream reader from "+path);
        return new InputStreamReader(
                Objects.requireNonNull(
                        mainTest.class.getResourceAsStream(path)
                ),
                StandardCharsets.UTF_8);
    }

    private static <T> T fromJsonResource(String path, Class<T> tClass){
        return GSON.fromJson(getInputStreamReaderFromResources(path), tClass);
    }


    private static Path existsOrCreateFolder(String thisYear) {
        Path resolve = DB_FOLDER.resolve(thisYear);
        if (!Files.exists(resolve)){
            try {
                Path result = Files.createDirectory(resolve);
                System.out.println(result);

                return result;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(thisYear+" exits");
        if (resolve.getFileName().equals(Path.of("temp")) ||
                resolve.getFileName().equals(Path.of("responses")) ) return resolve;
        System.out.println("not temp or responses");
        System.out.println(thisYear.equals("music 2025"));
        System.out.println("with database?");
        System.out.println(DATABASE.getDirectoriesTable().get(thisYear).get());
        System.out.println(DATABASE.getDirectoriesTable().get(thisYear).isEmpty());
        if (DATABASE.getDirectoriesTable().get(thisYear).isEmpty()) {
            System.out.println("adding to database");
            addNewFolder(thisYear);
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
        FilesResponsesRepository filesResponsesRepository = FilesResponsesRepository.from(files,responses,new HashSet<>(sources));

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

    private static void addResponseToDatabaseRepositoryResponsesFolder(List<FileSongResponseSpotifyTrackSearchResponse> results) {
        System.out.println("results:"+results.size());
        results.stream()
                .peek(r -> JSON_PROCESSOR.write(r.result(),RESPONSES_FOLDER.resolve(r.response().nameWithExtension())))
                .peek(r-> DATABASE.getResponsesTable().insert(r.response()))
                .forEach(r->REPOSITORY.getFilesResponsesRepository().addResponse(r.fileSong(),r.response()));
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
                .map(aa -> DATABASE.getAlbumArtists().insert(aa))
                .forEach(aa->REPOSITORY.getItemsRepository().addArtistsAlbum(aa.artistsId(),aa.albumId()));
    }

    private static void addArtistsGenres(List<Genre.ItemGenre> list){
        list.stream().peek(ag-> DATABASE.getItemGenres().insert(ag)).forEach(
                ag->REPOSITORY.getGenresRepository().addArtistGenres(ag.item(),ag.genre())
        );
    }

    private static void addAlbumsGenres(List<Genre.ItemGenre> list){
        list.stream().peek(ag-> DATABASE.getItemGenres().insert(ag)).forEach(
                ag->REPOSITORY.getGenresRepository().addAlbumGenres(ag.item(),ag.genre())
        );
    }

    private static void addGenre(String g) {
        REPOSITORY.getGenresRepository().addGenre(DATABASE.getGenresTable().insert(new Genre(g)));
    }

    private static void addTracks(List<FileSongResponseSpotifyTrack> results, ArtistsAndAlbumsResultListener.SearchResult albumsAndArtists) {
        List<SimpleItem.ItemUri> items = new ArrayList<>();
        Map<String, Track> tracksBySourceId = new HashMap<>();
        Map<String, Set<Genre>> trackGenresBySourceId = new HashMap<>();
        Map<String,List<Artist>> trackArtistsBySourceId = new HashMap<>();

        System.out.println("filling");
        fillListAndMapsOfTrackWithSpotifyTracksAndAlbumsAndArtists(results, albumsAndArtists, items, tracksBySourceId, trackGenresBySourceId, trackArtistsBySourceId);

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
                        RESPONSE_STATUS_UPDATER.updateStatus(Response.Status.added, r.response()))).toList();
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

    /**
     * FLOW /////////////////////// /////////////////////// /////////////////////// ///////////////////////
     */

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
            Flow actionFlow = chooseFilter();
            FilesResponsesRepository filteredByDirectory = REPOSITORY.getFilesResponsesRepository().filterByDirectory(read.directory());
            switch (actionFlow) {
                case NO_RESPONSE -> {
                    requestFileSongs(List.copyOf(filteredByDirectory.filesWithoutResponse()));
                    System.out.println("returned from requested");

                }

                case NOT_CHECKED -> {
                    List<FileSongResponseSpotifyTrackSearchResponse> list = filteredByDirectory.filterByResponseStatus(Response.Status.not_checked).toMap().entrySet().stream()
                            .map(e -> new FileSongResponseSpotifyTrackSearchResponse(
                                            e.getKey(),
                                            e.getValue(),
                                            readResponseOf(e.getValue())
                                    )
                            ).toList();
                    selection(list);
                }
                case NOT_CONTAINED -> {
                    FilesResponsesRepository filesResponsesRepository = filteredByDirectory.filterByResponseStatus(Response.Status.checked_not_contained);
                    notContainedFlow(filesResponsesRepository.toMap()); //todo...
                }

                case CONTAINED_NOT_ADDED -> {
                    FilesResponsesRepository filesResponsesRepository = filteredByDirectory.filterByResponseStatus(Response.Status.checked_contained);
                    Set<String> albumsId = new HashSet<>();
                    Set<String> artistsId = new HashSet<>();

                    List<FileSongResponseSpotifyTrack> list =
                            filesResponsesRepository.toMap().entrySet().stream()
                                .peek(entry-> System.out.println(DB_FOLDER.resolve(entry.getValue().directory()).resolve(entry.getValue().nameWithExtension())))
                                .map(entry ->
                                        new FileSongResponseSpotifyTrack(
                                            entry.getKey(),
                                            entry.getValue(),
                                            JSON_PROCESSOR.read(
                                                    SpotifyTrack.class,
                                                    DB_FOLDER.resolve(entry.getValue().directory()).resolve(entry.getValue().nameWithExtension()))
                                        ))
                                .peek(t-> System.out.println(t.spotifyTrack().getName()))
                                .toList();
                    extractArtistsAndAlbumsFromFileSongResponseSpotifyTrack(list,albumsId,artistsId);
                    startRequestForArtistsAndAlbums(albumsId,list,artistsId);
                }
            }
        }

        try {
            COUNT_DOWN_LATCH_FOR_FINISHED_FLOWS.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("ending token manager?");
        TOKEN_MANAGER.end();
    }

    private static SpotifyTrackSearchResponse readResponseOf(Response value) {
        return JSON_PROCESSOR.read(SpotifyTrackSearchResponse.class,RESPONSES_FOLDER.resolve(value.nameWithExtension()));
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
        BatchTrackSearch batchTrackSearch = new BatchTrackSearch(queries, TOKEN_MANAGER, 20, TRACK_SEARCH, createResponseObserverForFileSongRequest(fileSongs));
        batchTrackSearch.start();
        System.out.println("started track search, returning");
    }

    private static ResponsesObserver createResponseObserverForFileSongRequest(List<FileSong> fileSongs) {
        List<FileSongResponseSpotifyTrackSearchResponse> results = new ArrayList<>();
        return new ResponsesObserver() {
            @Override
            public void notify(String query, String responseBody) {
                Optional<FileSong> first = fileSongs.stream().filter(f -> f.request().equals(query)).findFirst();
                if (first.isPresent()) {
                    FileSong fileSong = first.get();
                    System.out.println("from:"+fileSong.name()+ " query:"+query);
                    Response response = RESPONSE_CREATOR.createFrom(fileSong);
                    SpotifyTrackSearchResponse spotifyTrackSearchResponse = JSON_PROCESSOR.fromJson(responseBody, SpotifyTrackSearchResponse.class);
                    results.add(new FileSongResponseSpotifyTrackSearchResponse(fileSong, response, spotifyTrackSearchResponse));
                } else {
                    System.err.println("query:"+query+" found no match");
                }
            }

            @Override
            public void finished() {
                System.out.println("batch search finished -> storing data...");
                addResponseToDatabaseRepositoryResponsesFolder(results);
                System.out.println("!!! data stored !!!");
                selection(results);
            }
        };
    }


    /**
     * 3. SELECTION /////////////////////// /////////////////////// ///////////////////////
     *
     */

    private static void selection(List<FileSongResponseSpotifyTrackSearchResponse> requestedData){
        List<FileSongResponseSpotifyTrack> results = new ArrayList<>();
        requestedData.forEach(data -> {
            if (data.result().tracks().items().isEmpty()){
                markAsCheckedNotContained(data);
            } else {
                // table view:
                System.out.println("\n\n///////////////// Table ///////////////////");
                System.out.println("entries for:"+ data.fileSong().name());
                cellViewFor(data.result());
                Optional<SpotifyTrack> selected =  selectTrack(data.result());
                selected.ifPresentOrElse(
                        spotifyTrack-> {
                            results.add(new FileSongResponseSpotifyTrack(data.fileSong(), data.response(), spotifyTrack));
                            markAsCheckedAndContained(data);
                            rewriteJson(data.response(), spotifyTrack);
                        },
                        () -> markAsCheckedNotContained(data)
                        );
            }
        });

        // no doppelgangers
        Set<String> albumsId = new HashSet<>();
        Set<String> artistsId = new HashSet<>();
        extractArtistsAndAlbumsFromFileSongResponseSpotifyTrack(results, albumsId, artistsId);

        // first request artists, then albums, so the observers need access to resources in the opposite order
        // when artist batch search is finished, it will call another function which will start the batch album search
        // when the batch album search is finished, it will call another function to add the tracks to the database,
        // given that both, artists and album will exist in the database

        startRequestForArtistsAndAlbums(albumsId, results, artistsId);
    }

    private static void extractArtistsAndAlbumsFromFileSongResponseSpotifyTrack(List<FileSongResponseSpotifyTrack> results, Set<String> albumsId, Set<String> artistsId) {
        results.forEach(r->{
            // filtering already existing in the database/repository
            if (REPOSITORY
                    .getItemsRepository()
                    .getItemUriID(Database.ItemSource.spotify, SimpleItem.ItemType.album,r.spotifyTrack().getAlbum().getId())
                    .isEmpty()) {
                albumsId.add(r.spotifyTrack().getAlbum().getId());
            }
            r.spotifyTrack().getAlbum().getArtists().stream()
                    .map(SpotifySimplifiedObject::getId)
                    .filter(a-> REPOSITORY.getItemsRepository().getItemUriID(Database.ItemSource.spotify, SimpleItem.ItemType.artist,a).isEmpty())
                    .forEach(artistsId::add);
            r.spotifyTrack().getArtists().stream()
                    .map(SpotifySimplifiedObject::getId)
                    .filter(a-> REPOSITORY.getItemsRepository().getItemUriID(Database.ItemSource.spotify, SimpleItem.ItemType.artist,a).isEmpty())
                    .forEach(artistsId::add);
        });
    }

    private static void rewriteJson(Response response, SpotifyTrack spotifyTrack) {
        JSON_PROCESSOR.write(spotifyTrack,RESPONSES_FOLDER.resolve(response.nameWithExtension()));
    }

    private static void markAsCheckedAndContained(FileSongResponseSpotifyTrackSearchResponse data) {
        Response response = RESPONSE_STATUS_UPDATER.updateStatus(Response.Status.checked_contained,data.response());
        updateResponseFromNotChecked(data.fileSong(), response);
    }

    private static Optional<SpotifyTrack> selectTrack(SpotifyTrackSearchResponse result) {

        System.out.println("chose track by id or mark as 'not contained':");
        String s = readText();
        if (result.tracks().items().stream().anyMatch(t-> t.getId().equals(s))) {
            return result.tracks().items().stream().filter(t-> t.getId().equals(s)).findFirst();
        } else if (s.equals("not contained")) {
            return Optional.empty();
        } else {
            System.out.println("wrong input. write a track id or 'not contained' without '' ");
            return selectTrack(result);
        }
    }

    private static void cellViewFor(SpotifyTrackSearchResponse result) {
        result.tracks().items().forEach(i -> {
            System.out.println("////////////////////////////////////");
            System.out.println("id: "+i.getId());
            System.out.println("name: "+i.getName());
            System.out.println("spotify: "+i.getExternal_urls().spotify());
            System.out.println("album id: "+i.getAlbum().getId());
            System.out.println("album spotify: "+i.getAlbum().getExternal_urls().spotify());
            System.out.println("first artist id: "+i.getArtists().getFirst().getId());
            System.out.println("first artist spotify: "+i.getArtists().getFirst().getExternal_urls().spotify());
        });
    }

    private static void markAsCheckedNotContained(FileSongResponseSpotifyTrackSearchResponse data) {
        Response response = RESPONSE_STATUS_UPDATER.updateStatus(Response.Status.checked_not_contained,data.response());
        updateResponseFromNotChecked(data.fileSong(), response);
    }



    /**
     * 4. SEARCH ARTISTS AND ALBUMS, THEN ADD TRACK
     */

    private static void startRequestForArtistsAndAlbums(Set<String> albumsId, List<FileSongResponseSpotifyTrack> results, Set<String> artistsId) {
        ArtistsAndAlbumsResultListener listener = createSearchResultListenerForBatches();

        BatchSearch batchAlbumSearch =
                new BatchAlbumSearch(
                        List.copyOf(albumsId),
                        TOKEN_MANAGER,
                        20,
                        MULTIPLE_ALBUM_SEARCH,
                        createResponseObserverForAlbumsRequest(results, listener));
        BatchSearch batchArtistSearch =
                new BatchArtistSearch(
                        List.copyOf(artistsId),
                        TOKEN_MANAGER,
                        20,
                        MULTIPLE_ARTIST_SEARCH,
                        createResponseObserverForArtistsRequest(batchAlbumSearch, listener));

        if (!artistsId.isEmpty()) {
            System.out.println("starting artists search");
            batchArtistSearch.start();
        }
        else {
            System.out.println("no new artists, starting albums search");
            batchAlbumSearch.start();
        }
    }

    private static ArtistsAndAlbumsResultListener createSearchResultListenerForBatches() {
        return new ArtistsAndAlbumsResultListener() {
            final List<SimpleItem.ItemUri> artistsUris = new ArrayList<>();
            final List<SimpleItem.ItemUri> albumsUris = new ArrayList<>();
            final List<Artist> artists = new ArrayList<>();
            final List<Album> albums = new ArrayList<>();
            @Override
            public void setArtists(List<Artist> artists) {
                this.artists.addAll(artists);
            }

            @Override
            public void setAlbums(List<Album> albums) {
                this.albums.addAll(albums);
            }

            @Override
            public void setArtistURIs(List<SimpleItem.ItemUri> uris) {
                this.artistsUris.addAll(uris);
            }

            @Override
            public void setAlbumURIs(List<SimpleItem.ItemUri> uris) {
                this.albumsUris.addAll(uris);
            }

            @Override
            public SearchResult get() {
                return new SearchResult(
                        this.artists.stream().collect(Collectors.toMap(SimpleItem::id,a->a)),
                        this.albums.stream().collect(Collectors.toMap(SimpleItem::id,a->a)),
                        this.artistsUris.stream().collect(Collectors.toMap(SimpleItem.ItemUri::sourceId, a->a)),
                        this.albumsUris.stream().collect(Collectors.toMap(SimpleItem.ItemUri::sourceId, a->a)));
            }
        };
    }

    private static ResponsesObserver createResponseObserverForAlbumsRequest(List<FileSongResponseSpotifyTrack> results, ArtistsAndAlbumsResultListener listener) {
        List<SimpleItem.ItemUri> items = new ArrayList<>();
        Map<String,List<Genre>> albumGenres = new HashMap<>();
        Map<String,List<Artist>> albumArtistMap = new HashMap<>();
        HashMap<String, Album> albums = new HashMap<>();
        return new ResponsesObserver() {
            @Override
            public void notify(String query, String responseBody) {
                System.out.println("query:"+query);
                SpotifyMultipleAlbums spotifyMultipleArtists = JSON_PROCESSOR.fromJson(responseBody, SpotifyMultipleAlbums.class);
                System.out.println(spotifyMultipleArtists.albums().size());
                spotifyMultipleArtists.albums().stream()
                        .peek(a->items.add(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.album, a.getId())))
                        .peek(a-> System.out.println("added as item temporally:"+a.getId()))
                        .peek(spotifyAlbum-> albumGenres.put(
                                spotifyAlbum.getId(),
                                List.copyOf(
                                        spotifyAlbum.getArtists().stream()
                                                .map(spotifyArtist ->
                                                        REPOSITORY
                                                                .getItemsRepository()
                                                                .getArtistUriByItem(
                                                                        Database.ItemSource.spotify,
                                                                        spotifyArtist.getId()
                                                                )
                                                )
                                                .filter(Optional::isPresent)
                                                .map(Optional::get)
                                                .peek(artist -> {
                                                            albumArtistMap.putIfAbsent(spotifyAlbum.getId(), new ArrayList<>());
                                                            albumArtistMap.get(spotifyAlbum.getId()).add(artist);
                                                        }
                                                )
                                                .flatMap(artist ->
                                                        REPOSITORY.getGenresRepository().artistGenres(artist).stream()
                                                )
                                                .collect(toSet())
                                )
                        )
                        )
                        .forEach(spotifyAlbum -> albums.put(spotifyAlbum.getId(),new SpotifyAlbumAdapter(spotifyAlbum)));
                System.out.println("added temporary albums");
            }

            @Override
            public void finished() {
                System.out.println("adding albums");
                List<SimpleItem.ItemUri> itemsWithId = addItems(items);
                Map<Integer,Album> albumMap = new HashMap<>();
                List<Album> albumList = itemsWithId.stream()
                        .map(i -> ITEM_ID_UPDATER.update(i.id(), albums.get(i.sourceId())))
                        .peek(a-> albumMap.put(a.id(),a))
                        .toList();
                addAlbum(albumList);
                addAlbumArtists(itemsWithId.stream()
                        .map(i->Map.entry(albumMap.get(i.id()),albumArtistMap.get(i.sourceId())))
                        .flatMap(e->e.getValue().stream().map(artist -> new Album.AlbumArtist(e.getKey().id(),artist.id())))
                        .toList()
                );
                addAlbumsGenres(getItemGenresFromItemUriAndSourceIdWithGenres(itemsWithId, albumGenres));
                listener.setAlbums(albumList);
                listener.setAlbumURIs(itemsWithId);
                System.out.println("adding tracks");
                addTracks(results,listener.get());
                //todo -> redirect to not Contained
                System.out.println("normal flow finished");
                COUNT_DOWN_LATCH_FOR_FINISHED_FLOWS.countDown();
                System.out.println("new flow?");
                //notContainedFlow();
            }
        };
    }


    private static void fillListAndMapsOfTrackWithSpotifyTracksAndAlbumsAndArtists(List<FileSongResponseSpotifyTrack> results,
                                                                                   ArtistsAndAlbumsResultListener.SearchResult albumsAndArtists,
                                                                                   List<SimpleItem.ItemUri> items,
                                                                                   Map<String, Track> tracksBySourceId,
                                                                                   Map<String, Set<Genre>> trackGenresBySourceId,
                                                                                   Map<String, List<Artist>> trackArtistsBySourceId) {
        results.forEach(
                fileSongResponseSpotifyTrack -> {
                    List<Artist> trackArtists = fileSongResponseSpotifyTrack.spotifyTrack().getArtists()
                            .stream().map(SpotifySimplifiedObject::getId).map(stringId -> {
                                if (albumsAndArtists.artistsUris().containsKey(stringId)) {
                                    System.out.println("from album artists uris");
                                    Artist artist = albumsAndArtists.artists().get(albumsAndArtists.artistsUris().get(stringId).id());
                                    System.out.println("artist"+artist);
                                    return artist;
                                }
                                REPOSITORY.getItemsRepository().getArtistUriByItem(Database.ItemSource.spotify, stringId).ifPresentOrElse(a-> System.out.println("artist:"+a.name()),()-> System.out.println("not found"));
                                return REPOSITORY.getItemsRepository().getArtistUriByItem(Database.ItemSource.spotify, stringId).orElseThrow(() -> new RuntimeException("for:" + fileSongResponseSpotifyTrack.fileSong().name() + " artist from spotify:" + stringId + " wasn't added"));
                            })
                            .toList();
                    Album trackAlbum;
                    if (albumsAndArtists.albumsUris().containsKey(fileSongResponseSpotifyTrack.spotifyTrack().getAlbum().getId())) trackAlbum = albumsAndArtists.albums().get(albumsAndArtists.albumsUris().get(fileSongResponseSpotifyTrack.spotifyTrack().getAlbum().getId()).id());
                    else trackAlbum =  REPOSITORY.getItemsRepository().getAlbumUriByItem(Database.ItemSource.spotify, fileSongResponseSpotifyTrack.spotifyTrack().getAlbum().getId()).orElseThrow(() -> new RuntimeException("for:" + fileSongResponseSpotifyTrack.fileSong().name() + " album from spotify:" + fileSongResponseSpotifyTrack.spotifyTrack().getAlbum().getId() + " wasn't added"));
                    Set<Genre> trackGenres = trackArtists.stream().flatMap(a-> REPOSITORY.getGenresRepository().artistGenres(a).stream()).collect(toSet());
                    Track track = new SpotifyTrackAdapter(fileSongResponseSpotifyTrack.spotifyTrack(), fileSongResponseSpotifyTrack.fileSong(), trackAlbum);

                    items.add(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.track, fileSongResponseSpotifyTrack.spotifyTrack().getId()));
                    tracksBySourceId.put(fileSongResponseSpotifyTrack.spotifyTrack().getId(),track);
                    trackGenresBySourceId.put(fileSongResponseSpotifyTrack.spotifyTrack().getId(),trackGenres);
                    trackArtistsBySourceId.put(fileSongResponseSpotifyTrack.spotifyTrack().getId(), trackArtists);

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

    private static ResponsesObserver createResponseObserverForArtistsRequest(BatchSearch batchAlbumSearch, ArtistsAndAlbumsResultListener listener) {
        Map<String,List<Genre>> artistGenres = new HashMap<>();
        List<SimpleItem.ItemUri> items = new ArrayList<>();
        HashMap<String, Artist> artists = new HashMap<>();
        return new ResponsesObserver() {
            @Override
            public void notify(String query, String responseBody) {
                System.out.println("query:"+query);
                SpotifyMultipleArtists spotifyMultipleArtists = JSON_PROCESSOR.fromJson(responseBody, SpotifyMultipleArtists.class);
                spotifyMultipleArtists.artists().stream()
                        .peek(a-> items.add(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.artist,a.getId())))
                        .peek(a-> a.getGenres().forEach(g->{
                            if (!REPOSITORY.getGenresRepository().containsGenre(new Genre(g))) {
                                System.out.println("adding genre:"+g);
                                addGenre(g);}
                            artistGenres.putIfAbsent(a.getId(),new ArrayList<>());
                            artistGenres.get(a.getId()).add(new Genre(g));
                        }))
                        .forEach(a->artists.put(a.getId(),new SpotifyArtistAdapter(a)));
                System.out.println("added to temporary lists and maps"+query);
            }

            @Override
            public void finished() {
                System.out.println("finished search for artists");
                List<SimpleItem.ItemUri> itemsWithId = addItems(items);
                System.out.println("added as item");
                List<Artist> list = itemsWithId
                        .stream()
                        .map(i -> ITEM_ID_UPDATER.update(i.id(), artists.get(i.sourceId())))
                        .toList();

                addArtists(list);

                System.out.println("added as artists");
                System.out.println(artistGenres);
                List<Genre.ItemGenre> itemGenresFromItemUriAndSourceIdWithGenres =
                        getItemGenresFromItemUriAndSourceIdWithGenres(itemsWithId, artistGenres);
                System.out.println("obtained genres");
                addArtistsGenres(itemGenresFromItemUriAndSourceIdWithGenres);
                System.out.println("added genres");
                listener.setArtistURIs(itemsWithId);
                listener.setArtists(list);
                batchAlbumSearch.start();
                System.out.println("added to data and repositories");
            }
        };
    }

    /**
     * 5. NOT CONTAINED
     */

    private static void notContainedFlow(Map<FileSong, Response> map) {
        /*  -´`'P.todo -> incoherence with current methods for normal flow,
             can't spread the "fromAnotherSource" once start the artist/album search
             */
        System.out.println("bye, in process...");
        /*
        List<String> trackIds = new ArrayList<>();
        Map<FileSong,Response> fromAnotherSource = new HashMap<>();

        notContained.forEach((fileSong,response)->{
            Optional<String> trackId = askForATrackIdOrMarkFromOtherSource();
            trackId.ifPresentOrElse(trackIds::add,()->fromAnotherSource.put(fileSong,response));
        });
         */

    }

    private static Optional<String> askForATrackIdOrMarkFromOtherSource() {
        System.out.println("enter a track ID from the API or mark as 'other' if it's not in the api data:");
        String s = readText();
        if (s.equals("other")) return Optional.empty();
        return Optional.of(s);
    }

}

