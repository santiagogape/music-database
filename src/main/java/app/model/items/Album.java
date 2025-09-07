package app.model.items;

import java.time.LocalDateTime;

public interface Album extends SimpleItem {
    AlbumType albumType();
    int tracks();
    LocalDateTime release();
    ReleasePrecision precision();
    String label();

    enum AlbumType {
        single, album, compilation
    }

    enum ReleasePrecision {
        year, month, day
    }

    record AlbumArtist(Integer albumId, Integer artistsId){}
}
