package dependencies.model.SQLite.tables.files;

import app.model.items.FileSong;
import app.model.utilities.database.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static Main.MainDatabase.LocalDateTimeFromString;
import static Main.MainDatabase.LocalDateTimeToString;


public class FilesTable implements Database.UpdateTableIntID<FileSong> {

    static final String id = "ID";
    static final String name = "NAME";
    static final String directory = "DIRECTORY";
    static final String creation = "CREATION";
    static final String title = "TITLE";
    static final String album = "ALBUM";
    static final String artists = "ARTISTS";
    static final String request = "REQUEST";

    private final Connection connection;

    public FilesTable(Connection connection) {
        this.connection = connection;
    }


    @Override
    public FileSong insert(FileSong item) {
        String sql = """
        INSERT INTO FILES(NAME, DIRECTORY, CREATION, TITLE, ALBUM, ARTISTS, REQUEST)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        System.out.println(connection);


        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.name());
            stmt.setString(2, item.directory());
            stmt.setString(3, LocalDateTimeToString(item.creation()));
            stmt.setString(4, item.title());
            stmt.setString(5, item.album());
            stmt.setString(6, item.artists());
            stmt.setString(7, item.request());

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
            public LocalDateTime creation() {
                return item.creation();
            }

            @Override
            public Integer id() {
                return ID;
            }

            @Override
            public String directory() {
                return item.directory();
            }

            @Override
            public String name() {
                return item.name();
            }

            @Override
            public ItemType type() {
                return item.type();
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
        String directory = rs.getString(FilesTable.directory);
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
                    public LocalDateTime creation() {
                        return LocalDateTimeFromString(creation);
                    }

                    @Override
                    public Integer id() {
                        return id;
                    }

                    @Override
                    public String directory() {
                        return directory;
                    }

                    @Override
                    public String name() {
                        return name;
                    }

                    @Override
                    public ItemType type() {
                        return ItemType.file;
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

    @Override
    public FileSong update(FileSong item) {
        String sql = """
                UPDATE RESPONSES SET NAME = ?,
                 DIRECTORY = ? WHERE id = ?""";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.name());
            pstmt.setString(2, item.directory());
            pstmt.setInt(3, item.id());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return item;
    }
}
