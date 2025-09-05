package app.model.repositories;

import app.model.items.Album;
import app.model.items.Artist;
import app.model.items.Genre;
import app.model.items.Track;

import java.util.Map;
import java.util.Set;

public class GenresRepository {

    private final Map<Integer, Set<Genre>> artistGenres;
    private final Map<Integer, Set<Genre>> albumGenres;
    private final Map<Integer, Set<Genre>> trackGenres;
    private final Set<Genre> genres;

    public GenresRepository(Map<Integer, Set<Genre>> artistGenres,
                            Map<Integer, Set<Genre>> albumGenres,
                            Map<Integer, Set<Genre>> trackGenres,
                            Set<Genre> genres) {
        this.artistGenres = artistGenres;
        this.albumGenres = albumGenres;
        this.trackGenres = trackGenres;
        this.genres = genres;
    }

    public Set<Genre> artistGenres(Artist artist){
        return artistGenres.get(artist.id());
    }

    public Set<Genre> albumGenres(Album album){
        return albumGenres.get(album.id());
    }

    public Set<Genre> trackGenres(Track track){
        return trackGenres.get(track.id());
    }

    public Set<Genre> getGenres() {
        return genres;
    }

}
