package dependencies.model.SQLite.tables.files;

import app.model.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SourcesTable implements Database.TableIntID<Integer> {

    private final Connection connection;

    public SourcesTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Integer insert(Integer item) {
        String sql = """
        INSERT INTO DIRECTORIES(NAME)
        VALUES (?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM DIRECTORIES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in Files table"); // true si borró alguna fila
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Integer> get(Integer id) {
        String sql = "SELECT * FROM FILES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Integer> query(String sql) {
        List<Integer> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<Integer> file = Optional.of(rs.getInt(1));
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<Integer> all() {
        return query("SELECT * FROM DIRECTORIES");
    }

    @Override
    public List<Integer> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM DIRECTORIES LIMIT -1 OFFSET ?";
        List<Integer> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<Integer> file = Optional.of(rs.getInt(1));
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
