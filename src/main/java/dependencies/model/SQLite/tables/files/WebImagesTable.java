package dependencies.model.SQLite.tables.files;

import app.model.items.ImageRef;
import app.model.utilities.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WebImagesTable implements Database.TableIntID<ImageRef.ItemImageRef> {

    private final Connection connection;
    static final String item = "OBJECT";
    static final String source = "SOURCE";
    static final String width = "WIDTH";
    static final String height = "HEIGHT";

    public WebImagesTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM WEB_IMAGES WHERE OBJECT = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in WEB_IMAGES table"); // true si borró alguna fila
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ImageRef.ItemImageRef> get(Integer id) {
        String sql = "SELECT * FROM WEB_IMAGES WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return dataToItemImageRef(id, rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Optional<ImageRef.ItemImageRef> dataToItemImageRef(Integer id, ResultSet rs) throws SQLException {
        String path = rs.getString(WebImagesTable.source);
        int height = rs.getInt(WebImagesTable.height);
        int width = rs.getInt(WebImagesTable.width);
        return Optional.of(new ImageRef.ItemImageRef(id,path,height,width));
    }

    @Override
    public ImageRef.ItemImageRef insert(ImageRef.ItemImageRef item) {
        String sql = """
        INSERT INTO WEB_IMAGES(OBJECT, SOURCE, WIDTH, HEIGHT)
        VALUES (?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.item());
            stmt.setString(2, item.source());
            stmt.setInt(3, item.width());
            stmt.setInt(4, item.height());
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
    public List<ImageRef.ItemImageRef> query(String sql) {
        List<ImageRef.ItemImageRef> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<ImageRef.ItemImageRef> file = dataToItemImageRef(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private Optional<ImageRef.ItemImageRef> dataToItemImageRef(ResultSet rs) throws SQLException {
        return dataToItemImageRef(rs.getInt(item),rs);
    }

    @Override
    public List<ImageRef.ItemImageRef> all() {return query("SELECT * FROM WEB_IMAGES");
    }

    @Override
    public List<ImageRef.ItemImageRef> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM WEB_IMAGES LIMIT -1 OFFSET ?";
        List<ImageRef.ItemImageRef> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<ImageRef.ItemImageRef> file = dataToItemImageRef(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
