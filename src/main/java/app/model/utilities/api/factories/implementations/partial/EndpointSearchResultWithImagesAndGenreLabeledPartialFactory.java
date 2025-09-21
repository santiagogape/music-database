package app.model.utilities.api.factories.implementations.partial;

import app.model.utilities.api.factories.interfaces.EndpointSearchResultWithImagesAndGenreLabeledFactory;

import java.util.*;

public class EndpointSearchResultWithImagesAndGenreLabeledPartialFactory<T> extends EndpointSearchResultWithImagesPartialFactory<T> implements EndpointSearchResultWithImagesAndGenreLabeledFactory<T> {
    protected final Set<String> genres = new HashSet<>();
    protected final Map<String, List<String>> itemGenres = new HashMap<>();

    @Override
    public void addItemGenre(String sourceId, String genre) {
        genres.add(genre);
        itemGenres.computeIfAbsent(sourceId, _ -> new ArrayList<>()).add(genre);
    }
}
