package app.model.items;

import java.time.LocalDateTime;
import java.util.List;

public interface Album extends SimpleItem {
    AlbumType albumType();
    LocalDateTime release();
    String precision();

    enum AlbumType {
        single, album, compilation, ep
    }
}
