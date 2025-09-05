package dependencies.model.SQLite.tables.files;

import app.model.items.FileReference;
import app.model.utilities.database.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dependencies.model.SQLite.MusicSQLiteDatabase.LocalDateTimeFromString;
import static dependencies.model.SQLite.MusicSQLiteDatabase.LocalDateTimeToString;

public class ResponsesTable implements Database.Table<FileReference> {

    static final String id = "ID";
    static final String name = "NAME";
    static final String path = "PATH";
    static final String creation = "CREATION";
    private final Connection connection;

    public ResponsesTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public FileReference insert(FileReference item) {
        String sql = """
        INSERT INTO RESPONSES(ID, NAME, PATH, CREATION)
        VALUES (?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.id());
            stmt.setString(2, item.name());
            stmt.setString(3, item.path());
            stmt.setString(4, LocalDateTimeToString(item.creation()));

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Not inserted RESPONSES.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM RESPONSES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in RESPONSES table"); // true si borró alguna fila
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public FileReference update(Integer id, FileReference item) {
        return null;
    }

    @Override
    public Optional<FileReference> get(Integer id) {
        String sql = "SELECT * FROM RESPONSES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return dataToFileReference(id, rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        return Optional.empty();
    }

    private static Optional<FileReference> dataToFileReference(Integer id, ResultSet rs) throws SQLException {
        String name = rs.getString(ResponsesTable.name);
        String path = rs.getString(ResponsesTable.path);
        String creation = rs.getString(ResponsesTable.creation);

        return Optional.of(new FileReference() {
            @Override
            public Integer id() {
                return id;
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
            public String name() {
                return name;
            }

            @Override
            public ItemType type() {
                return ItemType.response;
            }
        });
    }

    private static Optional<FileReference> dataToFileReference(ResultSet rs) throws SQLException {
        int id = rs.getInt(ResponsesTable.id);
        return dataToFileReference(id, rs);
    }

    @Override
    public List<FileReference> query(String sql) {
        List<FileReference> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<FileReference> file = dataToFileReference(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<FileReference> all() {
        return query("SELECT * FROM RESPONSES");
    }

    @Override
    public List<FileReference> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM RESPONSES LIMIT -1 OFFSET ?";
        List<FileReference> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<FileReference> file = dataToFileReference(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
