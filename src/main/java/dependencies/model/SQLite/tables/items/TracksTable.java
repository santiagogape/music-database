package dependencies.model.SQLite.tables.items;

import app.model.items.Track;
import app.model.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dependencies.model.SQLite.MusicSQLiteDatabase.LocalDateTimeFromString;
import static dependencies.model.SQLite.MusicSQLiteDatabase.LocalDateTimeToString;

public class TracksTable implements Database.Table<Track> {

    private final Connection connection;

    public TracksTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Track insert(Track item) {
        String sql = """
        INSERT INTO TRACKS(ID, NAME, ALBUM, NUMBER, PATH, CREATION)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.id());
            stmt.setString(2, item.name());
            stmt.setInt(3, item.albumId());
            stmt.setInt(4, item.number());
            stmt.setString(5, item.path());
            stmt.setString(6, LocalDateTimeToString(item.creation()));

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Not inserted TRACKS.");
            }
            return item;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM TRACKS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in ARTISTS table");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    @Deprecated
    public Track update(Integer id, Track item) {
        return null;
    }

    @Override
    public Optional<Track> get(Integer id) {
        String sql = "SELECT * FROM TRACKS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(dataToTrack(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private static Track dataToTrack(ResultSet rs) throws SQLException {
        Integer id = rs.getInt(1);
        String name = rs.getString(2);
        Integer album = rs.getInt(3);
        int number = rs.getInt(4);
        String path = rs.getString(5);
        String creation = rs.getString(6);

        return new Track() {

            @Override
            public Integer albumId() {
                return album;
            }

            @Override
            public int number() {
                return number;
            }

            @Override
            public String path() {
                return path;
            }

            @Override
            public LocalDateTime creation() {
                return LocalDateTimeFromString(creation);
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
                return ItemType.track;
            }
        };

    }

    @Override
    public List<Track> query(String sql) {
        List<Track> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<Track> file = Optional.of(dataToTrack(rs));
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<Track> all() {
        return query("SELECT * FROM TRACKS");
    }

    @Override
    public List<Track> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM TRACKS LIMIT -1 OFFSET " + offset;
        return query(sql);
    }
}
