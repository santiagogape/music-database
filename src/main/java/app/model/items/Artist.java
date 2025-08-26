package app.model.items;

import java.util.List;

public interface Artist extends SimpleItem {
    List<String> genres();
}
