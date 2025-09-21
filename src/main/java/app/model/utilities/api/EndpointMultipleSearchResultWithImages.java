package app.model.utilities.api;

import app.model.items.ImageRef;

import java.util.List;
import java.util.Map;

public interface EndpointMultipleSearchResultWithImages<T> extends EndpointMultipleSearchResultBasic<T> {
    Map<String, List<ImageRef>> images();
}
