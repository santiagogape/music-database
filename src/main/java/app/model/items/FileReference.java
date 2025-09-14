package app.model.items;

import java.time.LocalDateTime;

public interface FileReference extends SimpleItem {
    String directory();
    LocalDateTime creation();
}
