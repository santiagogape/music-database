package app.model.repositories;

import app.model.items.Album;
import app.model.items.Artist;
import app.model.items.Genre;
import app.model.items.Track;

import java.util.Map;
import java.util.List;

public class GenresRepository {

    private final Map<Integer, List<Genre>> artistGenres;
    private final Map<Integer, List<Genre>> albumGenres;
    private final Map<Integer, List<Genre>> trackGenres;
    private final List<Genre> genres;

    public GenresRepository(Map<Integer, List<Genre>> artistGenres,
                            Map<Integer, List<Genre>> albumGenres,
                            Map<Integer, List<Genre>> trackGenres,
                            List<Genre> genres) {
        this.artistGenres = artistGenres;
        this.albumGenres = albumGenres;
        this.trackGenres = trackGenres;
        this.genres = genres;
    }

    public List<Genre> artistGenres(Artist artist){
        if (artistGenres.containsKey(artist.id())) return artistGenres.get(artist.id());
        return List.of();

    }

    public List<Genre> albumGenres(Album album){
        return albumGenres.get(album.id());
    }

    public List<Genre> trackGenres(Track track){
        return trackGenres.get(track.id());
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public Map<Integer, List<Genre>> getArtistGenres() {
        return artistGenres;
    }

    public Map<Integer, List<Genre>> getAlbumGenres() {
        return albumGenres;
    }

    public Map<Integer, List<Genre>> getTrackGenres() {
        return trackGenres;
    }
}
