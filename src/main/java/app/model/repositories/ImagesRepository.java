package app.model.repositories;

import app.model.items.ImageRef;
import app.model.items.ItemImage;
import app.model.items.SimpleItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImagesRepository {


    private final Map<Integer, List<ItemImage>> local;
    private final  Map<Integer, List<ImageRef.ItemImageRef>> web;

    public ImagesRepository(List<ItemImage> local, List<ImageRef.ItemImageRef> web) {
        this.local = local.stream().collect(Collectors.groupingBy(ItemImage::item));
        this.web = web.stream().collect(Collectors.groupingBy(ImageRef.ItemImageRef::item));
    }

    public Map<Integer, List<ItemImage>> getLocal() {
        return local;
    }

    public Map<Integer, List<ImageRef.ItemImageRef>> getWeb() {
        return web;
    }

    public Optional<List<ItemImage>> getLocalImagesFromItem(SimpleItem item){
        return Optional.ofNullable(local.get(item.id()));
    }

    public Optional<List<ImageRef.ItemImageRef>> getWebImagesFromItem(SimpleItem item){
        return Optional.ofNullable(web.get(item.id()));
    }

}
