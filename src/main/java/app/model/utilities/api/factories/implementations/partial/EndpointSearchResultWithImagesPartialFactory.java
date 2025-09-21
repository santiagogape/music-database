package app.model.utilities.api.factories.implementations.partial;

import app.model.items.ImageRef;
import app.model.utilities.api.factories.interfaces.EndpointSearchResultWithImagesFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndpointSearchResultWithImagesPartialFactory<T> extends EndpointSearchResultBasicPartialFactory<T> implements EndpointSearchResultWithImagesFactory<T> {
    protected final Map<String, List<ImageRef>> images = new HashMap<>();

    @Override
    public void addItemImageRef(String sourceId, ImageRef ref) {
        images.computeIfAbsent(sourceId, _ -> new ArrayList<>()).add(ref);
    }
}
