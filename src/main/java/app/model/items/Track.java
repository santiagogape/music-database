package app.model.items;


public interface Track extends FileReference {
    Integer albumId();
    int number();

    record TrackArtist(Integer trackId, Integer artistsId){}
}
