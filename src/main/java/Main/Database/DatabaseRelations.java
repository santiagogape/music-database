package Main.Database;

import dependencies.model.SQLite.tables.items.relations.AlbumArtistsTable;
import dependencies.model.SQLite.tables.items.relations.ItemGenresTable;
import dependencies.model.SQLite.tables.items.relations.TracksArtistsTable;

import java.sql.Connection;

public class DatabaseRelations {

    private final AlbumArtistsTable albumArtists;
    private final TracksArtistsTable trackArtists;
    private final ItemGenresTable itemGenres;

    public DatabaseRelations(Connection connection) {
        albumArtists = new AlbumArtistsTable(connection);
        trackArtists = new TracksArtistsTable(connection);
        itemGenres = new ItemGenresTable(connection);
    }

    public AlbumArtistsTable getAlbumArtists() {
        return albumArtists;
    }

    public TracksArtistsTable getTrackArtists() {
        return trackArtists;
    }

    public ItemGenresTable getItemGenres() {
        return itemGenres;
    }
}
