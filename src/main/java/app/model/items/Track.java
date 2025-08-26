package app.model.items;

import java.util.List;

public interface Track extends SimpleItem {
    String title();
    Album album();
    int number();
    List<Artist> artists();
    List<String> genres();
}
