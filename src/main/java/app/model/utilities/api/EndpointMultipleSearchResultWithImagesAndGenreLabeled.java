package app.model.utilities.api;

import app.model.items.Genre;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EndpointMultipleSearchResultWithImagesAndGenreLabeled<T> extends EndpointMultipleSearchResultWithImages<T> {
    Set<Genre> genres();

    Map<String, List<Genre>> itemGenres();
}
