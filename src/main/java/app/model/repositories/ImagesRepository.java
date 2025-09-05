package app.model.repositories;

import app.model.items.ItemImage;
import app.model.items.SimpleItem;

import java.util.List;
import java.util.Map;

public class ImagesRepository {

    private final Map<Integer, List<ItemImage>> images;

    public ImagesRepository(Map<Integer, List<ItemImage>> images) {
        this.images = images;
    }

    public Map<Integer, List<ItemImage>> getImages() {
        return images;
    }

    public List<ItemImage> imagesOf(SimpleItem item){
        return images.get(item.id());
    }
}
