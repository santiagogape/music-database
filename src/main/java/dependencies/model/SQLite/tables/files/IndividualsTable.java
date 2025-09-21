package dependencies.model.SQLite.tables.files;

import app.model.items.FileSong;
import app.model.utilities.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IndividualsTable implements Database.TableIntID<FileSong.Individual> {

    private final Connection connection;

    public IndividualsTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM INDIVIDUALS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) throw new SQLException(id +" not in SOURCES table");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FileSong.Individual> get(Integer id) {
        String sql = "SELECT * FROM INDIVIDUALS WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return dataToIndividual(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public FileSong.Individual insert(FileSong.Individual item) {
        String sql = """
        INSERT INTO INDIVIDUALS(ID,TRACK_ID)
        VALUES (?,?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, item.id());
            stmt.setString(2, item.trackId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    @Override
    public List<FileSong.Individual> query(String sql) {
        List<FileSong.Individual> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<FileSong.Individual> file = dataToIndividual(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private Optional<FileSong.Individual> dataToIndividual(ResultSet rs) throws SQLException {
        int anInt = rs.getInt(1);
        String string = rs.getString(2);
        return Optional.of(new FileSong.Individual(anInt,string));
    }

    @Override
    public List<FileSong.Individual> all() {
        return query("SELECT * FROM INDIVIDUALS");
    }

    @Override
    public List<FileSong.Individual> allWithOffset(Integer offset) {
        String sql = "SELECT * FROM INDIVIDUALS LIMIT -1 OFFSET ?";
        List<FileSong.Individual> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<FileSong.Individual> file = dataToIndividual(rs);
                    file.ifPresent(results::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
