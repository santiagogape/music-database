package dependencies.SQLite.tables;

import app.model.items.ImageRef;
import app.model.items.ItemImage;
import app.model.items.SimpleItem;
import app.model.utilities.database.Database;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dependencies.SQLite.MusicDatabase.LocalDateTimeFromString;
import static dependencies.SQLite.MusicDatabase.LocalDateTimeToString;

public class ObjectsTable implements Database.Table<SimpleItem> {

    private final Connection connection;
    private final Database.Table<ItemImage> images;
    static final String id = "ID";
    static final String path = "PATH";
    static final String name = "NAME";
    static final String type = "TYPE";
    static final String creation = "CREATION";

    public ObjectsTable(Connection connection, Database.Table<ItemImage> images) {
        this.connection = connection;
        this.images = images;
    }

    @Override
    public SimpleItem insert(SimpleItem item) {
        String sql = """
        INSERT INTO OBJECTS(PATH, NAME, TYPE, CREATION)
        VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.path());
            stmt.setString(2, item.name());
            stmt.setString(3, item.type().name());
            stmt.setString(4, LocalDateTimeToString(item.creation()));

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Not inserted OBJECTS.");
            }

            // ID autoincrement
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int ID = rs.getInt(1);
                    return updateSimpleItemWithID(item, ID);
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

    private SimpleItem updateSimpleItemWithID(SimpleItem item, int id) {
        return new SimpleItem() {
            @Override
            public ItemType type() {
                return item.type();
            }

            @Override
            public LocalDateTime creation() {
                return item.creation();
            }

            @Override
            public List<ImageRef> images() {
                return item.images();
            }

            @Override
            public Integer id() {
                return id;
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
    private Optional<SimpleItem> dataToSimpleItem(Integer id, ResultSet rs) throws SQLException {
        String path = rs.getString(ObjectsTable.path);
        String name = rs.getString(ObjectsTable.name);
        String type = rs.getString(ObjectsTable.type);
        String creation = rs.getString(ObjectsTable.creation);
        return Optional.of(new SimpleItem() {
            @Override
            public ItemType type() {
                return ItemType.valueOf(type);
            }

            @Override
            public LocalDateTime creation() {
                return LocalDateTimeFromString(creation);
            }

            @Override
            public List<ImageRef> images() {
                return imagesFromItem(id);
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

    private List<ImageRef> imagesFromItem(Integer id) {
        return images.query("SELECT * FROM IMAGES WHERE OBJECT = " + id).stream().map(i ->
                    new ImageRef(i.path(),i.width(),i.height())
                ).toList();
    }

    private Optional<SimpleItem> dataToSimpleItem(ResultSet rs) throws SQLException {
        return dataToSimpleItem(rs.getInt(ObjectsTable.id), rs);
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
    @Deprecated
    public SimpleItem update(Integer id, SimpleItem item) {
        return null;
    }

    @Override
    public Optional<SimpleItem> get(Integer id) {
        String sql = "SELECT * FROM OBJECTS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return dataToSimpleItem(id, rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<SimpleItem> query(String sql) {
        List<SimpleItem> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<SimpleItem> file = dataToSimpleItem(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<SimpleItem> all() {
        return query("SELECT * FROM OBJECTS");
    }

    @Override
    public List<SimpleItem> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM OBJECTS LIMIT -1 OFFSET ?";
        List<SimpleItem> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<SimpleItem> file = dataToSimpleItem(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
