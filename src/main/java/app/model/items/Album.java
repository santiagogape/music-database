package app.model.items;

import java.time.LocalDateTime;
import java.util.List;

public interface Album extends SimpleItem {
    AlbumType albumType();
    List<Track> tracks();
    LocalDateTime release();
    String precision();
    List<Artist> artists();

    enum AlbumType {
        single, album, compilation, ep
    }
}
