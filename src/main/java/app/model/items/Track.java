package app.model.items;

import java.util.List;

public interface Track extends SimpleItem {
    String title();
    Integer albumId();
    int number();
    List<String> genres();
}
