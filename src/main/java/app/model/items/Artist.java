package app.model.items;



public interface Artist extends SimpleItem {
    record ArtistAlbum(Integer artistId, Integer albumsId){}
}
