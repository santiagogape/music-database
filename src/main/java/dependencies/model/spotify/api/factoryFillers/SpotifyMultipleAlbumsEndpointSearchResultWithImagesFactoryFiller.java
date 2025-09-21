package dependencies.model.spotify.api.factoryFillers;

import app.model.items.Album;
import app.model.items.SimpleItem;
import app.model.utilities.api.EndpointMultipleSearchResultWithImages;
import app.model.utilities.api.factories.FactoryFiller;
import app.model.utilities.api.factories.implementations.EndpointSearchResultBasicWithImagesFactory;
import app.model.utilities.database.Database;
import dependencies.model.spotify.adapters.SpotifyAlbumAdapter;
import dependencies.model.spotify.items.SpotifyMultipleAlbums;

public class SpotifyMultipleAlbumsEndpointSearchResultWithImagesFactoryFiller
        implements FactoryFiller<
        SpotifyMultipleAlbums,
        Album,
        EndpointMultipleSearchResultWithImages<Album>,
        EndpointSearchResultBasicWithImagesFactory<Album>> {
    public SpotifyMultipleAlbumsEndpointSearchResultWithImagesFactoryFiller() {
    }

    @Override
    public void fill(SpotifyMultipleAlbums source, EndpointSearchResultBasicWithImagesFactory<Album> factory) {
        source.albums().forEach(album -> {
            factory.addUri(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.album, album.getId()));
            factory.addItem(album.getId(), new SpotifyAlbumAdapter(album));
            album.getImages().forEach(i->factory.addItemImageRef(album.getId(), i));
        });
    }
}
