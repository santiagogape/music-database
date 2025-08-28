package app.control.database;

import app.model.items.Genre;
import app.model.items.SimpleItem;
import app.model.utilities.database.Database;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UpdateDBObjectGenresTable {
    private final Database db;
    private final Database.Table<Genre.ItemGenre> table;

    public UpdateDBObjectGenresTable(Database db, Database.Table<Genre.ItemGenre> table) {
        this.db = db;
        this.table = table;
    }

    public List<Genre.ItemGenre> with(Map<SimpleItem, Set<Genre>> genres){
        List<Genre.ItemGenre> list = genres.entrySet().stream()
                .flatMap(entry ->
                        entry.getValue().stream()
                                .map(genre -> new AbstractMap.SimpleEntry<>(entry.getKey(), genre))
                )
                .map(e -> new Genre.ItemGenre(e.getKey().id(), e.getValue()))
                .map(table::insert)
                .toList();
        db.commit();
        return list;
    }
}
