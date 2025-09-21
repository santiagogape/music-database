package app.model.utilities.api.factories.implementations.partial;

import app.model.items.SimpleItem;
import app.model.utilities.api.factories.interfaces.EndpointSearchResultBasicFactory;

import java.util.*;

public class EndpointSearchResultBasicPartialFactory<T> implements EndpointSearchResultBasicFactory<T> {
    protected final List<SimpleItem.ItemUri> uris = new ArrayList<>();
    protected final Map<String, T> items = new HashMap<>();

    @Override
    public void addUri(SimpleItem.ItemUri uri) {
        uris.add(uri);
    }

    @Override
    public void addItem(String sourceId, T item) {
        items.put(sourceId,item);
    }

}
