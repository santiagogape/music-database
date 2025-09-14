package dependencies.model.SQLite.tables.items;

import app.model.items.SimpleItem;
import app.model.utilities.database.Database;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObjectsTable implements Database.TableIntID<SimpleItem.ItemUri> {

    private final Connection connection;
    static final String id = "ID";
    static final String type = "TYPE";
    static final String source = "SOURCE";
    static final String idSource = "ID_SOURCE";

    public ObjectsTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public SimpleItem.ItemUri insert(SimpleItem.ItemUri item) {
        String sql = """
        INSERT INTO OBJECTS(TYPE, SOURCE, ID_SOURCE)
        VALUES (?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.type().name());
            stmt.setString(2, item.source().name());
            stmt.setString(3, item.sourceId());

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Not inserted OBJECTS.");
            }

            // ID autoincrement
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int ID = rs.getInt(1);
                    return updateItemURIWithID(item, ID);
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

    private SimpleItem.ItemUri updateItemURIWithID(SimpleItem.ItemUri item, int id) {
        return new SimpleItem.ItemUri(id, item.source(), item.type(), item.sourceId());
    }

    private Optional<SimpleItem.ItemUri> dataToItemUri(Integer id, ResultSet rs) throws SQLException {
        String type = rs.getString(ObjectsTable.type);
        String source = rs.getString(ObjectsTable.source);
        String sourceId = rs.getString(ObjectsTable.idSource);
        return Optional.of(new SimpleItem.ItemUri(id, Database.ItemSource.valueOf(source), SimpleItem.ItemType.valueOf(type), sourceId));
    }

    private Optional<SimpleItem.ItemUri> dataToItemUri(ResultSet rs) throws SQLException {
        return dataToItemUri(rs.getInt(ObjectsTable.id), rs);
    }


    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM OBJECTS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in OBJECTS table");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<SimpleItem.ItemUri> get(Integer id) {
        String sql = "SELECT * FROM OBJECTS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return dataToItemUri(id, rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<SimpleItem.ItemUri> query(String sql) {
        List<SimpleItem.ItemUri> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<SimpleItem.ItemUri> file = dataToItemUri(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<SimpleItem.ItemUri> all() {
        return query("SELECT * FROM OBJECTS");
    }

    @Override
    public List<SimpleItem.ItemUri> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM OBJECTS LIMIT -1 OFFSET " + offset;
        return query(sql);
    }
}
