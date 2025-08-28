package app.control.database;

import app.model.items.ItemImage;
import app.model.items.SimpleItem;
import app.model.utilities.database.Database;

import java.io.File;

import java.util.List;
import java.util.Map;

import static app.io.FileToModel.FileToItemImage;

public class UpdateDBImagesTable {

    private final Database db;
    private final Database.Table<ItemImage> table;

    public UpdateDBImagesTable(Database db, Database.Table<ItemImage> table) {
        this.db = db;
        this.table = table;
    }

    public List<ItemImage> with(Map<SimpleItem, File> images){
        List<ItemImage> list = images.entrySet().stream()
                .map(e -> table.insert(FileToItemImage(e)))
                .toList();
        db.commit();
        return list;
    }



}
