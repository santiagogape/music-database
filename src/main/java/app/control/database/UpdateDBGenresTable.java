package app.control.database;

import app.model.items.Genre;
import app.model.utilities.database.Database;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateDBGenresTable {
    private final Database db;
    private final Database.Table<Genre> table;

    public UpdateDBGenresTable(Database db, Database.Table<Genre> table) {
        this.db = db;
        this.table = table;
    }

    public Set<Genre> with(Set<Genre> genres){
        List<Genre> all = table.all();
        Set<Genre> collect = genres.stream().filter(g -> !all.contains(g))
                .map(table::insert).collect(Collectors.toSet());
        db.commit();
        return collect;

    }
}
