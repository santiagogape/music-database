package dependencies.SQLite.tables;

import app.model.items.SimpleItem;
import app.model.utilities.database.Database;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class ArtistsTable implements Database.Table<SimpleItem>  {

    private final Connection connection;

    public ArtistsTable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public SimpleItem insert(SimpleItem item) {
        return null;
    }

    @Override
    public void delete(Integer id) {

    }

    @Override
    public SimpleItem update(Integer id, SimpleItem item) {
        return null;
    }

    @Override
    public Optional<SimpleItem> get(Integer id) {
        return Optional.empty();
    }

    @Override
    public List<SimpleItem> query(String sql) {
        return List.of();
    }

    @Override
    public List<SimpleItem> all() {
        return List.of();
    }

    @Override
    public List<SimpleItem> allWithOffset(Integer offset) {
        return List.of();
    }
}
