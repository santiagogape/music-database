package app.model.items;


public interface SimpleItem {
    Integer id();
    String name();
    ItemType type();

    enum ItemType{ track, album, artist, image, playlist, file, response }
    record ItemUri(Integer id, ItemType type, String spotifyId){
        String spotifyUri(){
            return "spotify:"+ type.name() + ":" + spotifyId;
        }
    } //for OBJECTS table
}
