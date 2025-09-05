package app.model.items;

import java.time.LocalDateTime;

public interface FileReference extends SimpleItem {
    String path();
    LocalDateTime creation();
}
