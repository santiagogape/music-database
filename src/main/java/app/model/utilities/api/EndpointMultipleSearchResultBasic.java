package app.model.utilities.api;

import app.model.items.SimpleItem;

import java.util.List;
import java.util.Map;

public interface EndpointMultipleSearchResultBasic<T> {
    List<SimpleItem.ItemUri> itemsUris();

    Map<String, T> items();

}
