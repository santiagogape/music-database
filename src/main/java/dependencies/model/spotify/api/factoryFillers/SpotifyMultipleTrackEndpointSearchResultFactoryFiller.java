package dependencies.model.spotify.api.factoryFillers;

import app.model.items.SimpleItem;
import app.model.utilities.api.EndpointMultipleSearchResultBasic;
import app.model.utilities.api.TrackSearchResultList;
import app.model.utilities.api.factories.FactoryFiller;
import app.model.utilities.api.factories.implementations.EndpointSearchResultBasicFactory;
import app.model.utilities.database.Database;
import dependencies.model.spotify.adapters.SpotifyTrackSearchResultAdapter;
import dependencies.model.spotify.items.SpotifyMultipleTracks;

public class SpotifyMultipleTrackEndpointSearchResultFactoryFiller
        implements FactoryFiller<
        SpotifyMultipleTracks,
        TrackSearchResultList.TrackSearchResult,
        EndpointMultipleSearchResultBasic<TrackSearchResultList.TrackSearchResult>,
        EndpointSearchResultBasicFactory<TrackSearchResultList.TrackSearchResult>> {
    public SpotifyMultipleTrackEndpointSearchResultFactoryFiller() {
    }

    @Override
    public void fill(SpotifyMultipleTracks source, EndpointSearchResultBasicFactory<TrackSearchResultList.TrackSearchResult> factory) {
        source.tracks().forEach(TrackSearchResult -> {
            factory.addUri(new SimpleItem.ItemUri(0, Database.ItemSource.spotify, SimpleItem.ItemType.track, TrackSearchResult.getId()));
            factory.addItem(TrackSearchResult.getId(), SpotifyTrackSearchResultAdapter.from(TrackSearchResult));
        });
    }
}

