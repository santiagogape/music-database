package dependencies.model.spotify.items;

import dependencies.model.spotify.items.full.SpotifyTrack;

import java.util.List;

public record SpotifyMultipleTracks(List<SpotifyTrack> tracks) {
}
