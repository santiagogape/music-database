package dependencies.model.SQLite.tables.files;

import app.model.utilities.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DirectoriesTable implements Database.TableStringID<String> {

    private final Connection connection;

    public DirectoriesTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public String insert(String item) {
        String sql = """
        INSERT INTO DIRECTORIES(NAME)
        VALUES (?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM DIRECTORIES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in Files table"); // true si borró alguna fila
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> get(String id) {
        String sql = "SELECT * FROM FILES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<String> query(String sql) {
        List<String> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<String> file = Optional.of(rs.getString(1));
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<String> all() {
        return query("SELECT * FROM DIRECTORIES");
    }

    @Override
    public List<String> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM DIRECTORIES LIMIT -1 OFFSET ?";
        List<String> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<String> file = Optional.of(rs.getString(1));
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
