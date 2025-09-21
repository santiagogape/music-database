package app.model.utilities.api.factories.interfaces;

import app.model.items.SimpleItem;

public interface EndpointSearchResultBasicFactory<T> {
    void addUri(SimpleItem.ItemUri uri);
    void addItem(String sourceId, T item);

}
