package app.model.items;

public record ImageRef(String url, int height, int width) {
    public record ItemImageRef(Integer item, String source, int height, int width){}
}
