package app.model.items;

import java.time.LocalDateTime;
import java.util.List;

public interface SimpleItem extends FileReference {
    ItemType type();
    LocalDateTime creation();
    List<ImageRef> images();

    enum ItemType{
        track, album, artist, image
    }
}
