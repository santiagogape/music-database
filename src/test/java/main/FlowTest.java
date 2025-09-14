package main;

import Main.MainDatabase;
import Main.MainRepository;
import app.control.TokenManager;
import app.control.api.*;
import app.control.files.ProcessMetadataToRequestString;
import app.control.files.actors.JsonWriter;
import app.control.files.actors.MP3Processor;
import app.model.items.*;
import app.model.repositories.FilesResponsesRepository;
import app.model.repositories.GenresRepository;
import app.model.repositories.ImagesRepository;
import app.model.repositories.ItemsRepository;
import app.model.utilities.database.Database;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dependencies.control.GsonJsonWriter;
import dependencies.control.JAudioTaggerMP3MetadataReader;
import dependencies.model.SQLite.MusicSQLiteDatabase;
import dependencies.spotify.control.SpotifyTokenManager;
import dependencies.spotify.control.search.SpotifyMultipleAlbumSearch;
import dependencies.spotify.control.search.SpotifyMultipleArtistSearch;
import dependencies.spotify.control.search.SpotifyTrackSearch;
import dependencies.spotify.model.adapters.SpotifyAlbumAdapter;
import dependencies.spotify.model.adapters.SpotifyArtistAdapter;
import dependencies.spotify.model.adapters.SpotifyTrackAdapter;
import dependencies.spotify.model.auth.ClientCredentials;
import dependencies.spotify.model.auth.TokenRequest;
import dependencies.spotify.model.items.SpotifyMultipleArtists;
import dependencies.spotify.model.items.SpotifyTrackSearchResponse;
import dependencies.spotify.model.items.full.SpotifyTrack;
import dependencies.spotify.model.items.simplified.SpotifyMultipleAlbums;
import dependencies.spotify.model.items.simplified.SpotifySimplifiedObject;

import java.io.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static app.model.items.SimpleItem.ItemType.response;
import static java.lang.Thread.sleep;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.*;

public class FlowTest {

    //UTILITIES
    static final Scanner scanner = new Scanner(System.in);
    static final HttpClient client = HttpClient.newHttpClient();
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static final JsonWriter jsonWriter = new GsonJsonWriter(gson);
    static final MP3Processor mp3Processor = new MP3Processor(new JAudioTaggerMP3MetadataReader(),new ProcessMetadataToRequestString());
    static final CountDownLatch onFinishFlows = new CountDownLatch(1);


    //API
    static final ClientCredentials credentials =
            fromJsonResource("/client-credentials.json", ClientCredentials.class);
    static final TokenManager tokenManager =
            new SpotifyTokenManager(client, new TokenRequest(credentials));
    static final Search trackSearch = new SpotifyTrackSearch(client);
    //DATABASE
    static final Path dbFolder = Path.of("C:\\Users\\santi\\Desktop\\musica");
    static final String dbName = "music.db";
    static final MainDatabase database = new MainDatabase(new MusicSQLiteDatabase(dbFolder.toString(),dbName));
    static final Path temp = existsOrCreateFolder("temp");
    static final Path responses = existsOrCreateFolder("responses");


    //Repositories
    static final MainRepository repository = new MainRepository();

    //CLASS,RECORD,ENUM
    record FilesFromDirectory(String directory, boolean areNew, List<FileSong> files){}
    record FileSongResponseSpotifyTrackSearchResponse(FileSong fileSong, Response response, SpotifyTrackSearchResponse result){}
    enum Flow {
        noResponse("0: files which haven't been requested"),
        notChecked("1: files whose response haven't been checked"),
        notContained("2: files whose response doesn't contain the track searched"),
        containedNotAdded("3: files whose response has been checked but haven't been added");
        public String description() { return description;}
        public final String description;
        Flow(String description) { this.description = description;}
    }
    record FileSongResponseSpotifyTrack(FileSong fileSong, Response response, SpotifyTrack spotifyTrack){}
    interface ArtistsAndAlbumsResultListener {
        void setArtists(List<Artist> artists);
        void setAlbums(List<Album> albums);
        void setArtistURIs(List<SimpleItem.ItemUri> uris);
        void setAlbumURIs(List<SimpleItem.ItemUri> uris);
        SearchResult get();
        record SearchResult(Map<Integer,Artist> artists, Map<Integer,Album> albums, Map<String,SimpleItem.ItemUri> artistsUris, Map<String,SimpleItem.ItemUri> albumsUris){}
    }



    // utility methods

    private static String jsonPathFrom(String query) {
        //todo -> FileSong ends With .mp3, Response with .json so -> correct that in the name of Response
        return responses.resolve(query+".json").toString();
    }

    private static InputStreamReader getInputStreamReaderFromResources(String path) {
        System.out.println("inputStream reader from "+path);
        return new InputStreamReader(
                Objects.requireNonNull(
                        mainTest.class.getResourceAsStream(path)
                ),
                StandardCharsets.UTF_8);
    }

    private static <T> T fromFileJsonToObject(Path path, Class<T> tClass){
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonResource(String path, Class<T> tClass){
        return gson.fromJson(getInputStreamReaderFromResources(path), tClass);
    }


    private static Path existsOrCreateFolder(String thisYear) {
        Path resolve = dbFolder.resolve(thisYear);
        if (!Files.exists(resolve)){
            try {
                Path result = Files.createDirectory(resolve);
                System.out.println(result);

                return result;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (resolve.getFileName().equals(Path.of("temp")) ||
                resolve.getFileName().equals(Path.of("responses")) ) return resolve;
        if (database.getDirectoriesTable().get(thisYear).isEmpty()) addNewFolder(thisYear);
        return resolve;
    }

    private static String readText(){
        return scanner.nextLine();
    }

    private static int readInt(){
        return scanner.nextInt();
    }


    /**
     *  COMMANDS ////////////////////////////////////////////////////////////////////////////////
     */

    /**
     * read database and fill repositories
     */
    private static void readDatabaseAndFillRepositories() {
        final Database.UpdateTableIntID<FileSong> filesTable = database.getFilesTable();
        final Database.UpdateTableIntID<Response> responsesTable = database.getResponsesTable();
        final Database.TableStringID<String> directoriesTable = database.getDirectoriesTable();
        final Database.TableIntID<Integer> sourcesTable = database.getSourcesTable();

        Database.TableIntID<ItemImage> imagesTable = database.getImagesTable();
        Database.TableIntID<ImageRef.ItemImageRef> webImagesTable = database.getWebImagesTable();


        final Database.TableIntID<Artist> artistsTable = database.getArtistsTable();
        final Database.TableIntID<Album> albumsTable = database.getAlbumsTable();
        final Database.TableIntID<Track> tracksTable = database.getTracksTable();
        final Database.TableIntID<SimpleItem.ItemUri> objectsTable = database.getObjectsTable();

        final Database.TableIntID<Album.AlbumArtist> albumArtistsTable = database.getAlbumArtists();
        final Database.TableIntID<Genre.ItemGenre> itemGenresTable = database.getItemGenres();
        final Database.TableIntID<Track.TrackArtist> trackArtistsTable = database.getTrackArtists();

        Database.Table<Genre> genresTable = database.getGenresTable();

        List<Genre> genres = genresTable.all();

        List<FileSong> files = filesTable.all();
        List<Response> responses = responsesTable.all();
        List<String> directories = directoriesTable.all();
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
                                        SimpleItem.ItemUri::type
                                ))));
        FilesResponsesRepository filesResponsesRepository = FilesResponsesRepository.from(files,responses,new HashSet<>(sources));

        Map<Integer, List<Genre>> itemGenresIds = getItemGenresIds(itemGenres);

        GenresRepository genresRepository = new GenresRepository(
                getItemGenresFromItemUriAndSourceIdWithGenres(itemGenresIds, artists),
                getItemGenresFromItemUriAndSourceIdWithGenres(itemGenresIds, albums),
                getItemGenresFromItemUriAndSourceIdWithGenres(itemGenresIds, tracks),
                genres
        );

        repository.setDirectories(directories);
        repository.setGenresRepository(genresRepository);
        repository.setItemsRepository(itemsRepository);
        repository.setFilesResponsesRepository(filesResponsesRepository);
        repository.setImagesRepository(new ImagesRepository(localImages,webImages));
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
        List<FileSong> list = files.stream().map(f -> database.getFilesTable().insert(f)).toList();
        ArrayList<FileSong> fileSongs = new ArrayList<>(repository.getFilesResponsesRepository().fileSongList());
        fileSongs.addAll(list);
        repository.setFilesResponsesRepository(
                new FilesResponsesRepository(
                        FilesResponsesRepository.fileSongListToMapByDirectory(fileSongs),
                        repository.getFilesResponsesRepository().getResponses(),
                        repository.getFilesResponsesRepository().getFromAnotherSource()
                )
        );
        return list;
    }

    private static void updateResponse(Response response){
        ArrayList<Response> responses = new ArrayList<>(repository.getFilesResponsesRepository().responseList());
        boolean b = responses.removeIf(f -> Objects.equals(f.id(), response.id()));
        if (b) responses.add(response);
        database.getResponsesTable().update(response);
        repository.setFilesResponsesRepository(
                new FilesResponsesRepository(
                        repository.getFilesResponsesRepository().getFiles(),
                        FilesResponsesRepository.responseListToMapByDirectory(repository.getFilesResponsesRepository().getFiles(),responses),
                        repository.getFilesResponsesRepository().getFromAnotherSource()
                )
        );
    }

    private static void updateResponses(List<Response> responses){
        ArrayList<Response> list = new ArrayList<>(repository.getFilesResponsesRepository().responseList());
        List<Response> responseList = responses.stream()
                .filter(response -> list.removeIf(f -> Objects.equals(f.id(), response.id())))
                .peek(list::add)
                .peek(response -> database.getResponsesTable().update(response))
                .toList();
        repository.setFilesResponsesRepository(
                new FilesResponsesRepository(
                        repository.getFilesResponsesRepository().getFiles(),
                        FilesResponsesRepository.responseListToMapByDirectory(repository.getFilesResponsesRepository().getFiles(),responseList),
                        repository.getFilesResponsesRepository().getFromAnotherSource()
                )
        );
    }

    private static void addNewFolder(String thisYear) {
        database.getDirectoriesTable().insert(thisYear);
        ArrayList<String> strings = new ArrayList<>(repository.getDirectories());
        strings.add(thisYear);
        repository.setDirectories(strings);
    }

    private static void addResponseToDatabaseRepositoryResponsesFolder(List<FileSongResponseSpotifyTrackSearchResponse> results) {
        System.out.println("results:"+results.size());
        ArrayList<Response> set = new ArrayList<>(repository.getFilesResponsesRepository().responseList());
        results.stream()
                .peek(r -> jsonWriter.save(r.result(),jsonPathFrom(r.response().name())))
                .map(r->database.getResponsesTable().insert(r.response()))
                .peek(f-> System.out.println("response:"+f.name()+":"+f.id()+" stored in the database"))
                .forEach(set::add);
        System.out.println("adding to repository");
        repository.setFilesResponsesRepository(
                new FilesResponsesRepository(
                        repository.getFilesResponsesRepository().getFiles(),
                        FilesResponsesRepository.responseListToMapByDirectory(repository.getFilesResponsesRepository().getFiles(),set),
                        repository.getFilesResponsesRepository().getFromAnotherSource()
                )
        );
    }

    private static List<SimpleItem.ItemUri> addItems(List<SimpleItem.ItemUri> items) {
        List<SimpleItem.ItemUri> result = items.stream().map(i -> database.getObjectsTable().insert(i)).toList();
        System.out.println("added to database");
        Map<Database.ItemSource, Map<SimpleItem.ItemType, List<SimpleItem.ItemUri>>> collect = result.stream().collect(
                Collectors.groupingBy(SimpleItem.ItemUri::source,
                        Collectors.groupingBy(
                                SimpleItem.ItemUri::type
                        )));
        repository.getItemsRepository().getItems().forEach((source,itemsBySource)->
                {
                    if (!collect.containsKey(source)) collect.put(source,itemsBySource);
                    else {
                        itemsBySource.forEach((type,itemsByType)-> {
                            if (!collect.get(source).containsKey(type)) collect.get(source).put(type,itemsByType);
                            else collect.get(source).get(type).addAll(itemsByType);
                        });
                    }
                }
                );

        System.out.println("made collected items");
        repository.setItemsRepository(new ItemsRepository(
                repository.getItemsRepository().getArtists(),
                repository.getItemsRepository().getAlbums(),
                repository.getItemsRepository().getTracks(),
                repository.getItemsRepository().getArtistsAlbums(),
                repository.getItemsRepository().getAlbumsTracks(),
                repository.getItemsRepository().getTrackArtists(),
                collect
                ));
        return result;
    }

    private static void addArtists(List<Artist> artists) {
        Map<Integer, Artist> collect = artists.stream().map(a -> database.getArtistsTable().insert(a)).collect(toMap(SimpleItem::id, a -> a));
        collect.putAll(repository.getItemsRepository().getArtists());

        repository.setItemsRepository(new ItemsRepository(
                collect,
                repository.getItemsRepository().getAlbums(),
                repository.getItemsRepository().getTracks(),
                repository.getItemsRepository().getArtistsAlbums(),
                repository.getItemsRepository().getAlbumsTracks(),
                repository.getItemsRepository().getTrackArtists(),
                repository.getItemsRepository().getItems()
        ));
    }

    private static void addAlbum(List<Album> albums) {
        Map<Integer, Album> collect = albums.stream().map(a -> database.getAlbumsTable().insert(a)).collect(toMap(SimpleItem::id, a -> a));
        collect.putAll(repository.getItemsRepository().getAlbums());

        repository.setItemsRepository(new ItemsRepository(
                repository.getItemsRepository().getArtists(),
                collect,
                repository.getItemsRepository().getTracks(),
                repository.getItemsRepository().getArtistsAlbums(),
                repository.getItemsRepository().getAlbumsTracks(),
                repository.getItemsRepository().getTrackArtists(),
                repository.getItemsRepository().getItems()
        ));

    }

    private static void addAlbumArtists(List<Album.AlbumArtist> list) {
        Map<Integer, List<Integer>> collect = list.stream()
                .map(aa -> database.getAlbumArtists().insert(aa))
                .collect(
                    groupingBy(Album.AlbumArtist::artistsId, mapping(Album.AlbumArtist::albumId, toList()))
                );
        collect.putAll(repository.getItemsRepository().getArtistsAlbums());
        repository.setItemsRepository(
                new ItemsRepository(
                        repository.getItemsRepository().getArtists(),
                        repository.getItemsRepository().getAlbums(),
                        repository.getItemsRepository().getTracks(),
                        collect,
                        repository.getItemsRepository().getAlbumsTracks(),
                        repository.getItemsRepository().getTrackArtists(),
                        repository.getItemsRepository().getItems()
                )
        );
    }

    private static void addArtistsGenres(List<Genre.ItemGenre> list){
        HashMap<Integer, List<Genre>> integerListHashMap = new HashMap<>(repository.getGenresRepository().getArtistGenres());
        integerListHashMap.putAll(addItemGenres(list));
        repository.setGenresRepository(
                new GenresRepository(
                        integerListHashMap,
                        repository.getGenresRepository().getAlbumGenres(),
                        repository.getGenresRepository().getTrackGenres(),
                        repository.getGenresRepository().getGenres()
                )
        );
    }

    private static void addAlbumsGenres(List<Genre.ItemGenre> list){
        HashMap<Integer, List<Genre>> integerListHashMap = new HashMap<>(repository.getGenresRepository().getAlbumGenres());
        integerListHashMap.putAll(addItemGenres(list));
        repository.setGenresRepository(
                new GenresRepository(
                        repository.getGenresRepository().getArtistGenres(),
                        integerListHashMap,
                        repository.getGenresRepository().getTrackGenres(),
                        repository.getGenresRepository().getGenres()
                )
        );
    }


    private static Map<Integer, List<Genre>> addItemGenres(List<Genre.ItemGenre> list) {
        return list.stream()
                .map(ig -> database.getItemGenres().insert(ig))
                .map(ig -> Map.entry(ig.item(), ig.genre()))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    private static void addGenre(String g) {

        ArrayList<Genre> genres = new ArrayList<>(repository.getGenresRepository().getGenres());
        genres.add(database.getGenresTable().insert(new Genre(g)));
        repository.setGenresRepository(
                new GenresRepository(
                        repository.getGenresRepository().getArtistGenres(),
                        repository.getGenresRepository().getAlbumGenres(),
                        repository.getGenresRepository().getTrackGenres(),
                        genres
                )
        );
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
        System.out.println("\"artistsBySource\""+gson.toJson(trackArtistsBySourceId));
        List<Track> tracks = itemsWithId.stream()
                .peek(i->trackGenresBySourceId.get(i.sourceId()).forEach(g->trackGenres.add(new Genre.ItemGenre(i.id(),g))))
                .peek(i-> System.out.println(trackArtistsBySourceId.containsKey(i.sourceId())))
                .peek(i->trackArtistsBySourceId.get(i.sourceId()).forEach(a->trackArtists.add(new Track.TrackArtist(i.id(),a.id()))))
                .map(i -> updateTrackId(i.id(), tracksBySourceId.get(i.sourceId()))).toList();
        System.out.println("\"genres\""+gson.toJson(trackGenres));
        System.out.println("\"artists\""+gson.toJson(trackArtists));
        System.out.println("adding tracks to database");
        addTracks(tracks, trackGenres, trackArtists);

        //todo -> add all

        List<Response> responses = results.stream().map(FileSongResponseSpotifyTrack::response).map(FlowTest::markAsAdded).toList();
        updateResponses(responses);
    }

    private static Response markAsAdded(Response response) {
        return new Response() {
            @Override
            public Status status() {
                return Status.added;
            }

            @Override
            public String directory() {
                return response.directory();
            }

            @Override
            public LocalDateTime creation() {
                return response.creation();
            }

            @Override
            public Integer id() {
                return response.id();
            }

            @Override
            public String name() {
                return response.name();
            }

            @Override
            public ItemType type() {
                return ItemType.response;
            }
        };
    }

    private static void addTracks(List<Track> list, List<Genre.ItemGenre> trackGenres, List<Track.TrackArtist> trackArtists) {
        System.out.println("inserting");
        list.forEach(t->database.getTracksTable().insert(t));
        System.out.println("tracks");
        trackGenres.forEach(tg->database.getItemGenres().insert(tg));
        System.out.println("genres");
        trackArtists.forEach(ta->database.getTrackArtists().insert(ta));

        System.out.println("added");
        Map<Integer, Track> tracks = list.stream().collect(toMap(SimpleItem::id, t -> t));
        Map<Integer, List<Genre>> genres = trackGenres.stream().collect(groupingBy(Genre.ItemGenre::item, mapping(Genre.ItemGenre::genre, toList())));
        Map<Integer, List<Integer>> artists = trackArtists.stream().collect(groupingBy(Track.TrackArtist::trackId, mapping(Track.TrackArtist::artistsId, toList())));
        tracks.putAll(repository.getItemsRepository().getTracks());
        genres.putAll(repository.getGenresRepository().getTrackGenres());
        artists.putAll(repository.getItemsRepository().getTrackArtists());

        System.out.println("to repositories");
        repository.setGenresRepository(
                new GenresRepository(
                        repository.getGenresRepository().getArtistGenres(),
                        repository.getGenresRepository().getAlbumGenres(),
                        genres,
                        repository.getGenresRepository().getGenres()
                )
        );

        repository.setItemsRepository(
                new ItemsRepository(
                        repository.getItemsRepository().getArtists(),
                        repository.getItemsRepository().getAlbums(),
                        tracks,
                        repository.getItemsRepository().getArtistsAlbums(),
                        repository.getItemsRepository().getAlbumsTracks(),
                        artists,
                        repository.getItemsRepository().getItems()
                )
        );

    }


    /**
     * update items with ID after Adding Item to Objects.Table
     */

    private static Artist updateArtistId(Integer id, Artist artist){
        return new Artist() {
            @Override
            public Integer id() {
                return id;
            }

            @Override
            public String name() {
                return artist.name();
            }

            @Override
            public ItemType type() {
                return ItemType.artist;
            }
        };
    }
    private static Album updateAlbumId(Integer id, Album album){
        return new Album() {
            @Override
            public AlbumType albumType() {
                return album.albumType();
            }

            @Override
            public int tracks() {
                return album.tracks();
            }

            @Override
            public LocalDateTime release() {
                return album.release();
            }

            @Override
            public ReleasePrecision precision() {
                return album.precision();
            }

            @Override
            public String label() {
                return album.label();
            }

            @Override
            public Integer id() {
                return id;
            }

            @Override
            public String name() {
                return album.name();
            }

            @Override
            public ItemType type() {
                return ItemType.album;
            }
        };
    }
    private static Track updateTrackId(Integer id, Track track){
        return new Track() {
            @Override
            public Integer albumId() {
                return track.albumId();
            }

            @Override
            public int number() {
                return track.number();
            }

            @Override
            public String directory() {
                return track.directory();
            }

            @Override
            public LocalDateTime creation() {
                return track.creation();
            }

            @Override
            public Integer id() {
                return id;
            }

            @Override
            public String name() {
                return track.name();
            }

            @Override
            public ItemType type() {
                return ItemType.track;
            }
        };
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
        tokenManager.start();
        if (read.areNew()){
            List<FileSong> fileSongs = addFileSongs(read.files());
            requestFileSongs(fileSongs); // jump to selection(List<FileSongResponseSpotifyTrackSearchResponse> requestedData)
            System.out.println("returned from requested");
        } else {
            Flow actionFlow = chooseFilter();
            FilesResponsesRepository filteredByDirectory = repository.getFilesResponsesRepository().filterByDirectory(read.directory);
            switch (actionFlow) {
                case noResponse -> {
                    requestFileSongs(List.copyOf(filteredByDirectory.filesWithoutResponse()));
                    System.out.println("returned from requested");

                }

                case notChecked -> {
                    List<FileSongResponseSpotifyTrackSearchResponse> list = filteredByDirectory.filterByResponseStatus(Response.Status.not_checked).toMap().entrySet().stream()
                            .map(e -> new FileSongResponseSpotifyTrackSearchResponse(
                                            e.getKey(),
                                            e.getValue(),
                                            readResponseOf(e.getValue())
                                    )
                            ).toList();
                    selection(list);
                }
                case notContained -> {
                    FilesResponsesRepository filesResponsesRepository = filteredByDirectory.filterByResponseStatus(Response.Status.checked_not_contained);
                    notContainedFlow(filesResponsesRepository.toMap()); //todo...
                }

                case containedNotAdded -> {
                    FilesResponsesRepository filesResponsesRepository = filteredByDirectory.filterByResponseStatus(Response.Status.checked_contained);
                    Set<String> albumsId = new HashSet<>();
                    Set<String> artistsId = new HashSet<>();

                    List<FileSongResponseSpotifyTrack> list =
                            filesResponsesRepository.toMap().entrySet().stream()
                                .peek(entry-> System.out.println(dbFolder.resolve(entry.getKey().directory()).resolve(entry.getKey().name()).toString()))
                                .map(entry ->
                                        new FileSongResponseSpotifyTrack(
                                            entry.getKey(),
                                            entry.getValue(),
                                            fromFileJsonToObject(dbFolder.resolve(entry.getValue().directory()).resolve(jsonPathFrom(entry.getValue().name())),SpotifyTrack.class)
                                        ))
                                .peek(t-> System.out.println(t.spotifyTrack().getName()))
                                .toList();
                    extractArtistsAndAlbumsFromFileSongResponseSpotifyTrack(list,albumsId,artistsId);
                    startRequestForArtistsAndAlbums(albumsId,list,artistsId);
                }
            }
        }

        try {
            onFinishFlows.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("ending token manager?");
        tokenManager.end();
    }

    private static SpotifyTrackSearchResponse readResponseOf(Response value) {
        try (BufferedReader reader = Files.newBufferedReader(responses.resolve(value.name()))) {
            return gson.fromJson(reader,SpotifyTrackSearchResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
            return readFromTemp();
        } else if (Objects.equals(readText(), "folder")){
            return readFromFolder();
        } else {
            System.out.println("wrong");
            return readFromFiles();
        }
    }

    private static FilesFromDirectory readFromTemp() {
        // read from temp -> new fileSong, then move to this year folder
        String thisYear = "music "+ LocalDate.now().getYear();
        Path thisYearFolder = existsOrCreateFolder(thisYear);
        return new FilesFromDirectory(
                "temp",
                true,
                mp3Processor
                        .process(temp)
                        .stream()
                        .map(f->moveFileSongFromTemp(f, thisYearFolder))
                        .toList()
        );
    }

    private static FileSong moveFileSongFromTemp(FileSong fileSong, Path thisYearFolder) {
        try {
            Files.move(temp.resolve(fileSong.name()),thisYearFolder.resolve(fileSong.name()));
            return new FileSong() {
                @Override
                public String title() {
                    return fileSong.title();
                }

                @Override
                public String album() {
                    return fileSong.album();
                }

                @Override
                public String artists() {
                    return fileSong.artists();
                }

                @Override
                public String request() {
                    return fileSong.request();
                }

                @Override
                public String directory() {
                    return thisYearFolder.getFileName().toString();
                }

                @Override
                public LocalDateTime creation() {
                    return fileSong.creation();
                }

                @Override
                public Integer id() {
                    return fileSong.id();
                }

                @Override
                public String name() {
                    return fileSong.name();
                }

                @Override
                public ItemType type() {
                    return fileSong.type();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FilesFromDirectory readFromFolder() {
        try(Stream<Path> directories = Files.list(dbFolder).filter(Files::isDirectory).filter(p->p!=temp && p!=responses)) {
            List<Path> list = directories.toList();
            System.out.println("read from:");
            list.forEach(System.out::println);
            System.out.println("choose:");
            String input = readText();
            if (list.contains(dbFolder.resolve(input))) {
                if (repository.getDirectories().contains(input)) return readFromDirectory(input);
                return new FilesFromDirectory(input,true,mp3Processor.process(dbFolder.resolve(input)));
            } else {
                return readFromFolder();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FilesFromDirectory readFromDatabase() {
        System.out.println("read from one of this directories:");
        repository.getDirectories().forEach(System.out::println);
        String input = readText();
        if (repository.getDirectories().contains(input)) return readFromDirectory(input);
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
        BatchTrackSearch batchTrackSearch = new BatchTrackSearch(queries, tokenManager, 20, trackSearch, createResponseObserverForFileSongRequest(fileSongs));
        batchTrackSearch.start();
        System.out.println("startde track serach, returning");
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
                    Response response = createResponse(fileSong);
                    SpotifyTrackSearchResponse spotifyTrackSearchResponse = gson.fromJson(responseBody, SpotifyTrackSearchResponse.class);
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

    private static Response createResponse(FileSong fileSong) {
        return new Response(){
            @Override
            public Status status() {
                return Status.not_checked;
            }

            @Override
            public String directory() {
                return "responses";
            }

            @Override
            public LocalDateTime creation() {
                return now();
            }

            @Override
            public Integer id() {
                return fileSong.id();
            }

            @Override
            public String name() {
                return fileSong.name();
            }

            @Override
            public ItemType type() {
                return response;
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
            if (data.result.tracks().items().isEmpty()){
                markAsCheckedNotContained(data);
            } else {
                // table view:
                System.out.println("\n\n///////////////// Table ///////////////////");
                System.out.println("entries for:"+data.fileSong.name());
                cellViewFor(data.result);
                Optional<SpotifyTrack> selected =  selectTrack(data.result);
                selected.ifPresentOrElse(
                        spotifyTrack-> {
                            results.add(new FileSongResponseSpotifyTrack(data.fileSong(), data.response(), spotifyTrack));
                            markAsCheckedAndContained(data);
                            rewriteJson(data.response, spotifyTrack);
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
            if (repository
                    .getItemsRepository()
                    .getItemUriID(Database.ItemSource.spotify, SimpleItem.ItemType.album,r.spotifyTrack().getAlbum().getId())
                    .isEmpty()) {
                albumsId.add(r.spotifyTrack().getAlbum().getId());
            }
            r.spotifyTrack().getAlbum().getArtists().stream()
                    .map(SpotifySimplifiedObject::getId)
                    .filter(a->repository.getItemsRepository().getItemUriID(Database.ItemSource.spotify, SimpleItem.ItemType.artist,a).isEmpty())
                    .forEach(artistsId::add);
            r.spotifyTrack().getArtists().stream()
                    .map(SpotifySimplifiedObject::getId)
                    .filter(a->repository.getItemsRepository().getItemUriID(Database.ItemSource.spotify, SimpleItem.ItemType.artist,a).isEmpty())
                    .forEach(artistsId::add);
        });
    }

    private static void rewriteJson(Response response, SpotifyTrack spotifyTrack) {
        jsonWriter.save(spotifyTrack,jsonPathFrom(responses.resolve(response.name()).toString()));
    }

    private static void markAsCheckedAndContained(FileSongResponseSpotifyTrackSearchResponse data) {
        Response response = markAsCheckedAndContained(data.response);
        updateResponse(response);
    }

    private static Response markAsCheckedAndContained(Response response) {
        return new Response() {
            @Override
            public Status status() {
                return Status.checked_contained;
            }

            @Override
            public String directory() {
                return response.directory();
            }

            @Override
            public LocalDateTime creation() {
                return response.creation();
            }

            @Override
            public Integer id() {
                return response.id();
            }

            @Override
            public String name() {
                return response.name();
            }

            @Override
            public ItemType type() {
                return response.type();
            }
        };
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
        Response response = markAsCheckedNotContained(data.response);
        updateResponse(response);
    }

    private static Response markAsCheckedNotContained(Response response) {

        return new Response() {
            @Override
            public Status status() {
                return Status.checked_not_contained;
            }

            @Override
            public String directory() {
                return response.directory();
            }

            @Override
            public LocalDateTime creation() {
                return response.creation();
            }

            @Override
            public Integer id() {
                return response.id();
            }

            @Override
            public String name() {
                return response.name();
            }

            @Override
            public ItemType type() {
                return response.type();
            }
        };
    }


    /**
     * 4. SEARCH ARTISTS AND ALBUMS, THEN ADD TRACK
     */

    private static void startRequestForArtistsAndAlbums(Set<String> albumsId, List<FileSongResponseSpotifyTrack> results, Set<String> artistsId) {
        ArtistsAndAlbumsResultListener listener = createSearchResultListenerForBatches();

        BatchSearch batchAlbumSearch =
                new BatchAlbumSearch(
                        List.copyOf(albumsId),
                        tokenManager,
                        20,
                        new SpotifyMultipleAlbumSearch(client),
                        createResponseObserverForAlbumsRequest(results, listener));
        BatchSearch batchArtistSearch =
                new BatchArtistSearch(
                        List.copyOf(artistsId),
                        tokenManager,
                        20,
                        new SpotifyMultipleArtistSearch(client),
                        createResponseObserverForArtistsRequest(batchAlbumSearch, listener));

        if (!artistsId.isEmpty()) {
            System.out.println("starting artists search");
            batchArtistSearch.start();
        }
        else {
            System.out.println("starting albums search");
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
                SpotifyMultipleAlbums spotifyMultipleArtists = gson.fromJson(responseBody, SpotifyMultipleAlbums.class);
                System.out.println(spotifyMultipleArtists.albums().size());
                spotifyMultipleArtists.albums().stream()
                        .peek(a->items.add(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.album, a.getId())))
                        .peek(a-> System.out.println("added as item temporaly:"+a.getId()))
                        .peek(spotifyAlbum-> albumGenres.put(
                                spotifyAlbum.getId(),
                                List.copyOf(
                                        spotifyAlbum.getArtists().stream()
                                                .map(spotifyArtist ->
                                                        repository
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
                                                        repository.getGenresRepository().artistGenres(artist).stream()
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
                        .map(i -> updateAlbumId(i.id(), albums.get(i.sourceId())))
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
                onFinishFlows.countDown();
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
                                    System.out.println("from albumartistsuris");
                                    Artist artist = albumsAndArtists.artists().get(albumsAndArtists.artistsUris().get(stringId).id());
                                    System.out.println("artist"+artist);
                                    return artist;
                                }
                                repository.getItemsRepository().getArtistUriByItem(Database.ItemSource.spotify, stringId).ifPresentOrElse(a-> System.out.println("artist:"+a.name()),()-> System.out.println("not found"));
                                return repository.getItemsRepository().getArtistUriByItem(Database.ItemSource.spotify, stringId).orElseThrow(() -> new RuntimeException("for:" + fileSongResponseSpotifyTrack.fileSong.name() + " artist from spotify:" + stringId + " wasn't added"));
                            })
                            .toList();
                    Album trackAlbum;
                    if (albumsAndArtists.albumsUris().containsKey(fileSongResponseSpotifyTrack.spotifyTrack.getAlbum().getId())) trackAlbum = albumsAndArtists.albums().get(albumsAndArtists.albumsUris().get(fileSongResponseSpotifyTrack.spotifyTrack.getAlbum().getId()).id());
                    else trackAlbum =  repository.getItemsRepository().getAlbumUriByItem(Database.ItemSource.spotify, fileSongResponseSpotifyTrack.spotifyTrack.getAlbum().getId()).orElseThrow(() -> new RuntimeException("for:" + fileSongResponseSpotifyTrack.fileSong.name() + " album from spotify:" + fileSongResponseSpotifyTrack.spotifyTrack.getAlbum().getId() + " wasn't added"));
                    Set<Genre> trackGenres = trackArtists.stream().flatMap(a->repository.getGenresRepository().artistGenres(a).stream()).collect(toSet());
                    Track track = new SpotifyTrackAdapter(fileSongResponseSpotifyTrack.spotifyTrack, fileSongResponseSpotifyTrack.fileSong, trackAlbum);

                    items.add(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.track,fileSongResponseSpotifyTrack.spotifyTrack.getId()));
                    tracksBySourceId.put(fileSongResponseSpotifyTrack.spotifyTrack.getId(),track);
                    trackGenresBySourceId.put(fileSongResponseSpotifyTrack.spotifyTrack.getId(),trackGenres);
                    trackArtistsBySourceId.put(fileSongResponseSpotifyTrack.spotifyTrack.getId(), trackArtists);

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
                SpotifyMultipleArtists spotifyMultipleArtists = gson.fromJson(responseBody, SpotifyMultipleArtists.class);
                spotifyMultipleArtists.artists().stream()
                        .peek(a-> items.add(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.artist,a.getId())))
                        .peek(a-> a.getGenres().forEach(g->{
                            if (!repository.getGenresRepository().getGenres().contains(new Genre(g))) {
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
                        .map(i -> updateArtistId(i.id(), artists.get(i.sourceId())))
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
        /* todo -> incoherence with current methods for normal flow,
             can't spread the "fromAnotherSource" once start the artist/album search
             */
        System.out.println("bye, in process...");
        /*
        List<String> trackIds = new ArrayList<>();
        Map<FileSong,Response> fromAnotherSource = new HashMap<>();

        notContained.forEach((filesong,response)->{
            Optional<String> trackId = askForATrackIdOrMarkFromOtherSource();
            trackId.ifPresentOrElse(trackIds::add,()->fromAnotherSource.put(filesong,response));
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

