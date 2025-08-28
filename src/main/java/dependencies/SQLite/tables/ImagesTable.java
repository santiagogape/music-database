package dependencies.SQLite.tables;

import app.model.items.FileSong;
import app.model.items.ItemImage;
import app.model.utilities.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImagesTable implements Database.Table<ItemImage> {

    private final Connection connection;
    static final String item = "OBJECT";
    static final String number = "NUMBER";
    static final String path = "PATH";
    static final String width = "WIDTH";
    static final String height = "HEIGHT";

    public ImagesTable(Connection connection) {
        this.connection = connection;
    }

    private static Optional<ItemImage> dataToItemImage(ResultSet rs) throws SQLException {
        return dataToItemImage(rs.getInt(ImagesTable.item),rs);
    }
    private static Optional<ItemImage> dataToItemImage(Integer item, ResultSet rs) throws SQLException {
        String path = rs.getString(ImagesTable.path);
        int number = rs.getInt(ImagesTable.number);
        int height = rs.getInt(ImagesTable.height);
        int width = rs.getInt(ImagesTable.width);
        return Optional.of(new ItemImage() {
            @Override
            public Integer item() {
                return item;
            }

            @Override
            public int number() {
                return number;
            }

            @Override
            public String path() {
                return path;
            }

            @Override
            public int width() {
                return width;
            }

            @Override
            public int height() {
                return height;
            }
        });
    }

    @Override
    public ItemImage insert(ItemImage item) {
        String sql = """
        INSERT INTO IMAGES(OBJECT, NUMBER, PATH, WIDTH, HEIGHT)
        VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.item());
            stmt.setInt(2, item.number());
            stmt.setString(3, item.path());
            stmt.setInt(4, item.width());
            stmt.setInt(5, item.height());

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Not inserted IMAGES.");
            }
            return item;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM IMAGES WHERE OBJECT = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in IMAGES table"); // true si borró alguna fila
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ItemImage update(Integer id, ItemImage item) {
        return null;
    }

    @Override
    public Optional<ItemImage> get(Integer id) {
        String sql = "SELECT * FROM IMAGES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return dataToItemImage(id, rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<ItemImage> query(String sql) {
        List<ItemImage> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<ItemImage> file = dataToItemImage(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public List<ItemImage> all() {
        return query("SELECT * FROM IMAGES");
    }

    @Override
    public List<ItemImage> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM IMAGES LIMIT -1 OFFSET ?";
        List<ItemImage> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<ItemImage> file = dataToItemImage(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
