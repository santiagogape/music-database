package dependencies.model.SQLite.tables.others;

import app.model.items.Genre;
import app.model.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenresTable implements Database.Table<Genre> {

    private final Connection connection;

    public GenresTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Genre insert(Genre item) {
        String sql = """
        INSERT INTO GENRES(GENRE)
        VALUES (?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item.name());
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Not inserted into GENRES.");
            }
            return item;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public void delete(Integer id) {
        throw new UnsupportedOperationException("GENRES has no numeric ID. Use deleteByName instead.");
    }

    @Override
    @Deprecated
    public Genre update(Integer id, Genre item) {
        return null;
    }

    @Override
    @Deprecated
    public Optional<Genre> get(Integer id) {
        throw new UnsupportedOperationException("GENRES has no numeric ID. Use getByName instead.");
    }

    private static Genre dataToGenre(ResultSet rs) throws SQLException {
        String name = rs.getString("GENRE");
        return new Genre(name);
    }

    @Override
    public List<Genre> query(String sql) {
        List<Genre> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(dataToGenre(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<Genre> all() {
        return query("SELECT * FROM GENRES");
    }

    @Override
    public List<Genre> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM GENRES LIMIT -1 OFFSET " + offset;
        return query(sql);
    }
}
