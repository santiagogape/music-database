package dependencies.SQLite.tables;

import app.model.items.FileSong;
import app.model.utilities.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class FilesTable implements Database.Table<FileSong> {

    static final String id = "ID";
    static final String path = "PATH";
    static final String name = "NAME";
    static final String title = "TITLE";
    static final String album = "ALBUM";
    static final String artists = "ARTISTS";
    static final String request = "REQUEST";
    static final String creation = "CREATION";

    private final Connection connection;

    public FilesTable(Connection connection) {
        this.connection = connection;
    }


    @Override
    public FileSong insert(FileSong item) {
        String sql = """
        INSERT INTO FILES(PATH, NAME, TITLE, ALBUM, ARTISTS, REQUEST, CREATION)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.path());
            stmt.setString(2, item.name());
            stmt.setString(3, item.title());
            stmt.setString(4, item.album());
            stmt.setString(5, item.artists());
            stmt.setString(6, item.request());
            stmt.setString(7, item.creation());

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Not inserted FILES.");
            }

            // ID autoincrement
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int ID = rs.getInt(1);
                    return updateFileSongWithID(item, ID);
                } else {
                    throw new SQLException("No ID obtained");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileSong updateFileSongWithID(FileSong item, int ID) {
        return new FileSong() {
            @Override
            public String title() {
                return item.title();
            }

            @Override
            public String album() {
                return item.album();
            }

            @Override
            public String artists() {
                return item.artists();
            }

            @Override
            public String request() {
                return item.request();
            }

            @Override
            public String creation() {
                return item.creation();
            }

            @Override
            public Integer id() {
                return ID;
            }

            @Override
            public String path() {
                return item.path();
            }

            @Override
            public String name() {
                return item.name();
            }
        };
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM FILES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in Files table"); // true si borró alguna fila
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    @Deprecated
    public FileSong update(Integer id, FileSong item) {
        return null;
    }

    @Override
    public Optional<FileSong> get(Integer id) {
        String sql = "SELECT * FROM FILES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return dataToFileSong(id, rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private static Optional<FileSong> dataToFileSong(Integer id, ResultSet rs) throws SQLException {
        String path = rs.getString(FilesTable.path);
        String name = rs.getString(FilesTable.name);
        String title = rs.getString(FilesTable.title);
        String album = rs.getString(FilesTable.album);
        String artists = rs.getString(FilesTable.artists);
        String request = rs.getString(FilesTable.request);
        String creation = rs.getString(FilesTable.creation);
        return Optional.of(
                new FileSong() {
                    @Override
                    public String title() {
                        return title;
                    }

                    @Override
                    public String album() {
                        return album;
                    }

                    @Override
                    public String artists() {
                        return artists;
                    }

                    @Override
                    public String request() {
                        return request;
                    }

                    @Override
                    public String creation() {
                        return creation;
                    }

                    @Override
                    public Integer id() {
                        return id;
                    }

                    @Override
                    public String path() {
                        return path;
                    }

                    @Override
                    public String name() {
                        return name;
                    }
                });
    }
    private static Optional<FileSong> dataToFileSong(ResultSet rs) throws SQLException {
        return dataToFileSong(rs.getInt(FilesTable.id), rs);
    }

    @Override
    public List<FileSong> query(String sql) {
        List<FileSong> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<FileSong> file = dataToFileSong(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<FileSong> all() {
       return query("SELECT * FROM FILES");
    }

    @Override
    public List<FileSong> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM FILES LIMIT -1 OFFSET ?";
        List<FileSong> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<FileSong> file = dataToFileSong(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
