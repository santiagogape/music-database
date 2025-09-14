package dependencies.model.SQLite.tables.items;

import app.model.items.Album;
import app.model.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static Main.MainDatabase.LocalDateTimeFromString;
import static Main.MainDatabase.LocalDateTimeToString;

public class AlbumsTable implements Database.TableIntID<Album> {

    private final Connection connection;

    public AlbumsTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Album insert(Album item) {
        String sql = """
        INSERT INTO ALBUMS(ID, NAME, TYPE, TRACKS, RELEASE, RELEASE_PRECISION, LABEL)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.id());
            stmt.setString(2, item.name());
            stmt.setString(3, item.albumType().name());
            stmt.setInt(4, item.tracks());
            stmt.setString(5, LocalDateTimeToString(item.release()));
            stmt.setString(6, item.precision().name());
            stmt.setString(7, item.label());
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Not inserted OBJECTS.");
            }
            return item;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM ALBUMS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in ALBUMS table");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Album dataToAlbum(ResultSet rs) throws SQLException {
        Integer id = rs.getInt(1);
        String name = rs.getString(2);
        String type = rs.getString(3);
        int tracks = rs.getInt(4);
        String release = rs.getString(5);
        String precision = rs.getString(6);
        String label = rs.getString(7);

        return new Album() {
            @Override
            public AlbumType albumType() {
                return AlbumType.valueOf(type);
            }

            @Override
            public int tracks() {
                return tracks;
            }

            @Override
            public LocalDateTime release() {
                return LocalDateTimeFromString(release);
            }

            @Override
            public ReleasePrecision precision() {
                return ReleasePrecision.valueOf(precision);
            }

            @Override
            public String label() {
                return label;
            }

            @Override
            public Integer id() {
                return id;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public ItemType type() {
                return ItemType.album;
            }
        };
    }

    @Override
    public Optional<Album> get(Integer id) {
        String sql = "SELECT * FROM ALBUMS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(dataToAlbum(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Album> query(String sql) {
        List<Album> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<Album> file = Optional.of(dataToAlbum(rs));
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<Album> all() {
        return query("SELECT * FROM ALBUMS");
    }

    @Override
    public List<Album> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM ALBUMS LIMIT -1 OFFSET " + offset;
        return query(sql);
    }
}
