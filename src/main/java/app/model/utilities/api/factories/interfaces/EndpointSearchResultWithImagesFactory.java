package app.model.utilities.api.factories.interfaces;

import app.model.items.ImageRef;

public interface EndpointSearchResultWithImagesFactory<T> extends EndpointSearchResultBasicFactory<T> {
    void addItemImageRef(String sourceId, ImageRef ref);
}
