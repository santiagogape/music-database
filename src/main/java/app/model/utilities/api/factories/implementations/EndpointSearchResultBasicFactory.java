package app.model.utilities.api.factories.implementations;

import app.model.items.SimpleItem;
import app.model.utilities.api.EndpointMultipleSearchResultBasic;
import app.model.utilities.api.Factory;
import app.model.utilities.api.factories.implementations.partial.EndpointSearchResultBasicPartialFactory;

import java.util.List;
import java.util.Map;

public class EndpointSearchResultBasicFactory<T> extends EndpointSearchResultBasicPartialFactory<T> implements Factory<EndpointMultipleSearchResultBasic<T>> {
    @Override
    public EndpointMultipleSearchResultBasic<T> get() {
        List<SimpleItem.ItemUri> itemUris = List.copyOf(uris);
        Map<String, T> itemsMap = Map.copyOf(items);
        uris.clear();
        items.clear();
        return new EndpointMultipleSearchResultBasic<>() {
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
