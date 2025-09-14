package Main;

import app.model.items.*;
import app.model.utilities.database.Database;
import dependencies.model.SQLite.MusicSQLiteDatabase;
import dependencies.model.SQLite.tables.files.*;
import dependencies.model.SQLite.tables.items.AlbumsTable;
import dependencies.model.SQLite.tables.items.ArtistsTable;
import dependencies.model.SQLite.tables.items.ObjectsTable;
import dependencies.model.SQLite.tables.items.TracksTable;
import dependencies.model.SQLite.tables.items.relations.AlbumArtistsTable;
import dependencies.model.SQLite.tables.items.relations.ItemGenresTable;
import dependencies.model.SQLite.tables.items.relations.TracksArtistsTable;
import dependencies.model.SQLite.tables.others.GenresTable;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MainDatabase {


    public static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Database database;
    private final Database.UpdateTableIntID<FileSong> filesTable;
    private final Database.TableIntID<ItemImage> imagesTable;
    private final Database.UpdateTableIntID<Response> responsesTable;
    private final Database.TableIntID<Integer> sourcesTable;
    private final Database.TableStringID<String> directoriesTable;
    private final Database.TableIntID<SimpleItem.ItemUri> objectsTable;
    private final Database.TableIntID<Artist> artistsTable;
    private final Database.TableIntID<Album> albumsTable;
    private final Database.TableIntID<Track> tracksTable;
    private final Database.TableIntID<Album.AlbumArtist> albumArtists;
    private final Database.TableIntID<Track.TrackArtist> trackArtists;
    private final Database.TableIntID<Genre.ItemGenre> itemGenres;
    private final Database.Table<Genre> genresTable;


    private final Database.TableIntID<ImageRef.ItemImageRef> webImagesTable;


    public MainDatabase(MusicSQLiteDatabase database) {
        this.database = database;
        database.open(); // @REQUIRED 1
        database.initialize(); // @REQUIRED 2
        Connection connection = database.getConnection();
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        filesTable = new FilesTable(connection);
        imagesTable = new ImagesTable(connection);
        webImagesTable = new WebImagesTable(connection);
        responsesTable = new ResponsesTable(connection);
        sourcesTable = new SourcesTable(connection);
        directoriesTable = new DirectoriesTable(connection);
        objectsTable = new ObjectsTable(connection);
        artistsTable = new ArtistsTable(connection);
        albumsTable = new AlbumsTable(connection);
        tracksTable = new TracksTable(connection);
        albumArtists = new AlbumArtistsTable(connection);
        trackArtists = new TracksArtistsTable(connection);
        itemGenres = new ItemGenresTable(connection);
        genresTable = new GenresTable(connection);
    }

    public static String now(){
        return LocalDateTime.now().format(fmt);
    }

    public static String LocalDateTimeToString(LocalDateTime time){
        return time.format(fmt);
    }

    public static LocalDateTime LocalDateTimeFromString(String time){
         return LocalDateTime.parse(time, fmt);
    }

    public Database getDatabase() {
        return database;
    }

    public Database.UpdateTableIntID<FileSong> getFilesTable() {
        return filesTable;
    }

    public Database.TableIntID<ItemImage> getImagesTable() {
        return imagesTable;
    }

    public Database.TableIntID<ImageRef.ItemImageRef> getWebImagesTable() {
        return webImagesTable;
    }

    public Database.UpdateTableIntID<Response> getResponsesTable() {
        return responsesTable;
    }

    public Database.TableIntID<Integer> getSourcesTable() {
        return sourcesTable;
    }

    public Database.TableStringID<String> getDirectoriesTable() {
        return directoriesTable;
    }

    public Database.TableIntID<SimpleItem.ItemUri> getObjectsTable() {
        return objectsTable;
    }

    public Database.TableIntID<Artist> getArtistsTable() {
        return artistsTable;
    }

    public Database.TableIntID<Album> getAlbumsTable() {
        return albumsTable;
    }

    public Database.TableIntID<Track> getTracksTable() {
        return tracksTable;
    }

    public Database.TableIntID<Album.AlbumArtist> getAlbumArtists() {
        return albumArtists;
    }

    public Database.TableIntID<Track.TrackArtist> getTrackArtists() {
        return trackArtists;
    }

    public Database.TableIntID<Genre.ItemGenre> getItemGenres() {
        return itemGenres;
    }

    public Database.Table<Genre> getGenresTable() {
        return genresTable;
    }

    public void close(){
        database.commit();
        database.close();
    }
}
