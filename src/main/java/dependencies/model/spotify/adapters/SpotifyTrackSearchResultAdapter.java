package dependencies.model.spotify.adapters;

import app.model.utilities.api.TrackSearchResultList;
import dependencies.model.spotify.items.SpotifyExternalUrls;
import dependencies.model.spotify.items.full.SpotifyTrack;
import dependencies.model.spotify.items.simplified.SpotifySimplifiedObject;

public class SpotifyTrackSearchResultAdapter {
    public static TrackSearchResultList.TrackSearchResult from(SpotifyTrack result){
        return new TrackSearchResultList.TrackSearchResult(
                result.getId(),
                result.getAlbum().getId(),
                result.getAlbum().getArtists().stream().map(SpotifySimplifiedObject::getId).toList(),
                result.getArtists().stream().map(SpotifySimplifiedObject::getId).toList(),
                result.getExternal_urls().spotify(),
                result.getAlbum().getExternal_urls().spotify(),
                result.getAlbum().getArtists().stream()
                        .map(SpotifySimplifiedObject::getExternal_urls).map(SpotifyExternalUrls::spotify).toList(),
                result.getArtists().stream()
                        .map(SpotifySimplifiedObject::getExternal_urls).map(SpotifyExternalUrls::spotify).toList(),
                result.getName(),
                result.getTrackNumber()
        );
    }
}
