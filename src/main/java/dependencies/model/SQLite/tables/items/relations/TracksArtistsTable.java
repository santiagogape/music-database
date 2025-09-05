package dependencies.model.SQLite.tables.items.relations;

import app.model.items.Track;
import app.model.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TracksArtistsTable implements Database.Table<Track.TrackArtist> {

    private final Connection connection;

    public TracksArtistsTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Track.TrackArtist insert(Track.TrackArtist item) {
        String sql = """
        INSERT INTO TRACK_ARTISTS(TRACK, ARTIST)
        VALUES (?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.trackId());
            stmt.setInt(2, item.artistsId());
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Not inserted TRACK_ARTISTS.");
            }
            return item;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM TRACK_ARTISTS WHERE TRACK = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in TRACK_ARTISTS table");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public Track.TrackArtist update(Integer id, Track.TrackArtist item) {
        return null;
    }

    @Override
    public Optional<Track.TrackArtist> get(Integer id) {
        String sql = "SELECT * FROM TRACK_ARTISTS WHERE TRACK = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(dataToTrackArtist(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private static Track.TrackArtist dataToTrackArtist(ResultSet rs) throws SQLException {
        Integer track = rs.getInt(1);
        Integer artist = rs.getInt(2);
        return new Track.TrackArtist(track,artist);
    }

    @Override
    public List<Track.TrackArtist> query(String sql) {
        List<Track.TrackArtist> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<Track.TrackArtist> file = Optional.of(dataToTrackArtist(rs));
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<Track.TrackArtist> all() {
        return query("SELECT * FROM TRACK_ARTISTS");
    }

    @Override
    public List<Track.TrackArtist> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM TRACK_ARTISTS LIMIT -1 OFFSET " + offset;
        return query(sql);
    }
}
