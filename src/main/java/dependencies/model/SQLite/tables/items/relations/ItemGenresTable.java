package dependencies.model.SQLite.tables.items.relations;


import app.model.items.Genre;
import app.model.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import java.util.*;

public class ItemGenresTable implements Database.TableIntID<Genre.ItemGenre> {

    private final Connection connection;

    public ItemGenresTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Genre.ItemGenre insert(Genre.ItemGenre item) {
        String sql = """
        INSERT INTO OBJECT_GENRES(OBJECT, GENRE)
        VALUES (?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.item());
            stmt.setString(2, item.genre().name()); // usamos el campo "name" del record Genre
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Not inserted OBJECT_GENRES.");
            }
            return item;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM OBJECT_GENRES WHERE OBJECT = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException(id + " not in OBJECT_GENRES table");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Genre.ItemGenre> get(Integer id) {
        String sql = "SELECT * FROM OBJECT_GENRES WHERE OBJECT = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(dataToItemGenre(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private static Genre.ItemGenre dataToItemGenre(ResultSet rs) throws SQLException {
        Integer objectId = rs.getInt(1);
        String genreName = rs.getString(2);
        return new Genre.ItemGenre(objectId, new Genre(genreName));
    }

    @Override
    public List<Genre.ItemGenre> query(String sql) {
        List<Genre.ItemGenre> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(dataToItemGenre(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<Genre.ItemGenre> all() {
        return query("SELECT * FROM OBJECT_GENRES");
    }

    @Override
    public List<Genre.ItemGenre> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM OBJECT_GENRES LIMIT -1 OFFSET " + offset;
        return query(sql);
    }
}
