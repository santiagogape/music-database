package dependencies.model.SQLite.tables.items;

import app.model.items.Artist;
import app.model.utilities.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ArtistsTable implements Database.Table<Artist>  {

    private final Connection connection;

    public ArtistsTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Artist insert(Artist item) {
        String sql = """
        INSERT INTO ARTISTS(ID, NAME)
        VALUES (?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.id());
            stmt.setString(2, item.name());
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Not inserted ARTISTS.");
            }
            return item;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM ARTISTS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in ARTISTS table");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public Artist update(Integer id, Artist item) {
        return null;
    }

    @Override
    public Optional<Artist> get(Integer id) {
        String sql = "SELECT * FROM ARTISTS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(dataToArtist(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Artist> query(String sql) {
        List<Artist> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<Artist> file = Optional.of(dataToArtist(rs));
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private static Artist dataToArtist(ResultSet rs) throws SQLException {
        Integer id = rs.getInt(1);
        String name = rs.getString(2);

        return new Artist() {
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
                return ItemType.artist;
            }
        };
    }

    @Override
    public List<Artist> all() {
        return query("SELECT * FROM ARTISTS");
    }

    @Override
    public List<Artist> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM ARTISTS LIMIT -1 OFFSET " + offset;
        return query(sql);
    }
}
