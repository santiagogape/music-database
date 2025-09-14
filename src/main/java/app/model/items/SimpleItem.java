package app.model.items;


import app.model.utilities.database.Database;

public interface SimpleItem {
    Integer id();
    String name();
    ItemType type();

    enum ItemType{ track, album, artist, image, file, response }
    record ItemUri(Integer id, Database.ItemSource source, ItemType type, String sourceId){ } //for OBJECTS table
}
