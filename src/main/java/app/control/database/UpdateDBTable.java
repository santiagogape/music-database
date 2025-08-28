package app.control.database;

import app.model.utilities.database.Database;

import java.util.List;


/**
 * @param <T> Type of SimpleItem, Album, Track, Artist, FileSong
 */
public class UpdateDBTable<T> {
    private final Database db;
    private final Database.Table<T> table;

    public UpdateDBTable(Database db, Database.Table<T> table) {
        this.db = db;
        this.table = table;
    }

    public List<T> with(List<T> items){
        List<T> list = items.stream().map(table::insert).toList();
        db.commit();
        return list;
    }

}
