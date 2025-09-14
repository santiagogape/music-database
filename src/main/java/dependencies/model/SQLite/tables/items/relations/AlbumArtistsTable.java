package dependencies.model.SQLite.tables.items.relations;

import app.model.items.Album;
import app.model.utilities.database.Database;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import java.sql.*;
import java.util.*;

public class AlbumArtistsTable implements Database.TableIntID<Album.AlbumArtist> {

    private final Connection connection;

    public AlbumArtistsTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Album.AlbumArtist insert(Album.AlbumArtist item) {
        String sql = """
        INSERT INTO ALBUM_ARTISTS(ALBUM, ARTIST)
        VALUES (?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.albumId());
            stmt.setInt(2, item.artistsId());
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Not inserted ALBUM_ARTISTS.");
            }
            return item;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM ALBUM_ARTISTS WHERE ALBUM = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException(id + " not in ALBUM_ARTISTS table");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Album.AlbumArtist> get(Integer id) {
        String sql = "SELECT * FROM ALBUM_ARTISTS WHERE ALBUM = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(dataToAlbumArtist(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private static Album.AlbumArtist dataToAlbumArtist(ResultSet rs) throws SQLException {
        Integer album = rs.getInt(1);
        Integer artist = rs.getInt(2);
        return new Album.AlbumArtist(album, artist);
    }

    @Override
    public List<Album.AlbumArtist> query(String sql) {
        List<Album.AlbumArtist> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(dataToAlbumArtist(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<Album.AlbumArtist> all() {
        return query("SELECT * FROM ALBUM_ARTISTS");
    }

    @Override
    public List<Album.AlbumArtist> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM ALBUM_ARTISTS LIMIT -1 OFFSET " + offset;
        return query(sql);
    }
}