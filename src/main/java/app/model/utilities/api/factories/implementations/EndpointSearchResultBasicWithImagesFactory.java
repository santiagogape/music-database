package app.model.utilities.api.factories.implementations;

import app.model.items.ImageRef;
import app.model.items.SimpleItem;
import app.model.utilities.api.EndpointMultipleSearchResultWithImages;
import app.model.utilities.api.Factory;
import app.model.utilities.api.factories.implementations.partial.EndpointSearchResultWithImagesPartialFactory;

import java.util.List;
import java.util.Map;

public class EndpointSearchResultBasicWithImagesFactory<T> extends EndpointSearchResultWithImagesPartialFactory<T> implements Factory<EndpointMultipleSearchResultWithImages<T>> {
    @Override
    public EndpointMultipleSearchResultWithImages<T> get() {
        List<SimpleItem.ItemUri> itemUris = List.copyOf(uris);
        Map<String, T> itemsMap = Map.copyOf(items);
        Map<String, List<ImageRef>> itemImages = Map.copyOf(images);
        uris.clear();
        items.clear();
        images.clear();
        return new EndpointMultipleSearchResultWithImages<>() {
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
