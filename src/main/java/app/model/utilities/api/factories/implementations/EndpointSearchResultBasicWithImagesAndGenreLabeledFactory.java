package app.model.utilities.api.factories.implementations;

import app.model.items.Genre;
import app.model.items.ImageRef;
import app.model.items.SimpleItem;
import app.model.utilities.api.EndpointMultipleSearchResultWithImagesAndGenreLabeled;
import app.model.utilities.api.Factory;
import app.model.utilities.api.factories.implementations.partial.EndpointSearchResultWithImagesAndGenreLabeledPartialFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EndpointSearchResultBasicWithImagesAndGenreLabeledFactory<T> extends EndpointSearchResultWithImagesAndGenreLabeledPartialFactory<T> implements Factory<EndpointMultipleSearchResultWithImagesAndGenreLabeled<T>> {
    public EndpointSearchResultBasicWithImagesAndGenreLabeledFactory() {
    }

    @Override
    public EndpointMultipleSearchResultWithImagesAndGenreLabeled<T> get() {
        List<SimpleItem.ItemUri> itemUris = List.copyOf(uris);
        Map<String, T> itemsMap = Map.copyOf(items);
        Map<String, List<ImageRef>> itemImages = Map.copyOf(images);
        Map<String, List<Genre>> itemGenres = this.itemGenres.entrySet().stream().map(e -> Map.entry(e.getKey(), e.getValue().stream().map(Genre::new).toList())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Set<Genre> genres = this.genres.stream().map(Genre::new).collect(Collectors.toSet());
        uris.clear();
        items.clear();
        images.clear();
        this.genres.clear();
        this.itemGenres.clear();
        return new EndpointMultipleSearchResultWithImagesAndGenreLabeled<>() {
            @Override
            public Set<Genre> genres() {
                return genres;
            }

            @Override
            public Map<String, List<Genre>> itemGenres() {
                return itemGenres;
            }

            @Override
            public Map<String, List<ImageRef>> images() {
                return itemImages;
            }

            @Override
            public List<SimpleItem.ItemUri> itemsUris() {
                return itemUris;
            }

            @Override
            public Map<String, T> items() {
                return itemsMap;
            }
        };
    }
}
