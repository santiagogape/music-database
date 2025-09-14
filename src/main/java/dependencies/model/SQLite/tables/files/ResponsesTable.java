package dependencies.model.SQLite.tables.files;

import app.model.items.Response;
import app.model.utilities.database.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static Main.MainDatabase.LocalDateTimeFromString;
import static Main.MainDatabase.LocalDateTimeToString;

public class ResponsesTable implements Database.UpdateTableIntID<Response> {

    static final String id = "ID";
    static final String name = "NAME";
    static final String path = "PATH";
    static final String creation = "CREATION";
    static final String status = "STATUS";
    
    private final Connection connection;

    public ResponsesTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Response insert(Response item) {

        System.out.println(connection);


        System.out.println("inserting: "+item.id()+"-:-"+item.name()+" directory:"+item.directory());
        String sql = """
        INSERT INTO RESPONSES(ID, NAME, PATH, CREATION)
        VALUES (?, ?, ?, ?)
        """; //checked, contained and added -> default 'false'
        /*

         */
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.id());
            stmt.setString(2, item.name());
            stmt.setString(3, item.directory());
            stmt.setString(4, LocalDateTimeToString(item.creation()));
            System.out.println("inserting");
            System.out.println(stmt);
            int affected = stmt.executeUpdate();
            System.out.println("executed insertion");
            if (affected == 0) {
                System.err.println("not inserted");
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
    public Optional<Response> get(Integer id) {
        String sql = "SELECT * FROM RESPONSES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return dataToResponse(id, rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        return Optional.empty();
    }

    private static Optional<Response> dataToResponse(Integer id, ResultSet rs) throws SQLException {
        String name = rs.getString(ResponsesTable.name);
        String path = rs.getString(ResponsesTable.path);
        String creation = rs.getString(ResponsesTable.creation);
        String status = rs.getString(ResponsesTable.status);


        return Optional.of(new Response() {
            @Override
            public Status status() {
                return Status.valueOf(status);
            }

            @Override
            public Integer id() {
                return id;
            }

            @Override
            public String directory() {
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

    private static Optional<Response> dataToResponse(ResultSet rs) throws SQLException {
        int id = rs.getInt(ResponsesTable.id);
        return dataToResponse(id, rs);
    }

    @Override
    public List<Response> query(String sql) {
        List<Response> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<Response> file = dataToResponse(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<Response> all() {
        return query("SELECT * FROM RESPONSES");
    }

    @Override
    public List<Response> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM RESPONSES LIMIT -1 OFFSET ?";
        List<Response> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<Response> file = dataToResponse(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public Response update(Response item) {
        String sql = """
                UPDATE RESPONSES SET STATUS = ? WHERE id = ?
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.status().name());
            pstmt.setInt(2, item.id());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return item;
    }
}
