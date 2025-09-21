package app.model.repositories;

import app.model.items.ImageRef;
import app.model.items.ItemImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImagesRepository {


    private final Map<Integer, List<ItemImage>> local;
    private final  Map<Integer, List<ImageRef.ItemImageRef>> web;

    public ImagesRepository(List<ItemImage> local, List<ImageRef.ItemImageRef> web) {
        this.local = local.stream().collect(Collectors.groupingBy(ItemImage::item));
        this.web = web.stream().collect(Collectors.groupingBy(ImageRef.ItemImageRef::item));
    }

    public void addWebImage(ImageRef.ItemImageRef ref){
        web.computeIfAbsent(ref.item(), ArrayList::new).add(ref);
        System.out.println(web.get(ref.item()));
    }

    public void addLocalImageTo(ItemImage ref){
        local.computeIfAbsent(ref.item(), ArrayList::new).add(ref);
    }

}
