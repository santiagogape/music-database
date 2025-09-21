package dependencies.model.spotify.api.factoryFillers;

import app.model.items.Artist;
import app.model.items.SimpleItem;
import app.model.utilities.api.EndpointMultipleSearchResultWithImagesAndGenreLabeled;
import app.model.utilities.api.factories.FactoryFiller;
import app.model.utilities.api.factories.implementations.EndpointSearchResultBasicWithImagesAndGenreLabeledFactory;
import app.model.utilities.database.Database;
import dependencies.model.spotify.adapters.SpotifyArtistAdapter;
import dependencies.model.spotify.items.SpotifyMultipleArtists;

public class SpotifyMultipleArtistsEndpointSearchResultWithImagesAndGenreLabeledFactoryFiller
implements FactoryFiller<
        SpotifyMultipleArtists,
        Artist,
        EndpointMultipleSearchResultWithImagesAndGenreLabeled<Artist>,
        EndpointSearchResultBasicWithImagesAndGenreLabeledFactory<Artist>> {
    public SpotifyMultipleArtistsEndpointSearchResultWithImagesAndGenreLabeledFactoryFiller() {
    }

    @Override
    public void fill(SpotifyMultipleArtists source, EndpointSearchResultBasicWithImagesAndGenreLabeledFactory<Artist> factory) {
        source.artists().forEach(spotifyArtist -> {
            factory.addUri(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.artist, spotifyArtist.getId()));
            factory.addItem(spotifyArtist.getId(), new SpotifyArtistAdapter(spotifyArtist));
            spotifyArtist.getGenres().forEach(g->factory.addItemGenre(spotifyArtist.getId(), g));
            spotifyArtist.getImages().forEach(i->factory.addItemImageRef(spotifyArtist.getId(), i));
        });
    }
}
