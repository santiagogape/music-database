package app.model.utilities.api.factories.interfaces;

public interface EndpointSearchResultWithImagesAndGenreLabeledFactory<T> extends EndpointSearchResultWithImagesFactory<T> {
    void addItemGenre(String sourceId, String genre);
}
