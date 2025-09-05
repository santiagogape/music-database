package Main.Database;

import dependencies.model.SQLite.tables.items.AlbumsTable;
import dependencies.model.SQLite.tables.items.ArtistsTable;
import dependencies.model.SQLite.tables.items.ObjectsTable;
import dependencies.model.SQLite.tables.items.TracksTable;

import java.sql.Connection;

public class DatabaseItems {

    private final ObjectsTable objectsTable;
    private final ArtistsTable artistsTable;
    private final AlbumsTable albumsTable;
    private final TracksTable tracksTable;

    public DatabaseItems(Connection connection) {
        objectsTable = new ObjectsTable(connection);
        artistsTable = new ArtistsTable(connection);
        albumsTable = new AlbumsTable(connection);
        tracksTable = new TracksTable(connection);
    }

    public ObjectsTable getObjectsTable() {
        return objectsTable;
    }

    public ArtistsTable getArtistsTable() {
        return artistsTable;
    }

    public AlbumsTable getAlbumsTable() {
        return albumsTable;
    }

    public TracksTable getTracksTable() {
        return tracksTable;
    }
}
