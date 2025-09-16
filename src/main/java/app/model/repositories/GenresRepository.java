package app.model.repositories;

import app.model.items.Album;
import app.model.items.Artist;
import app.model.items.Genre;
import app.model.items.Track;

import java.util.ArrayList;
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
        if (albumGenres.containsKey(album.id())) return albumGenres.get(album.id());
        return List.of();
    }

    public List<Genre> trackGenres(Track track){
        if (trackGenres.containsKey(track.id())) return trackGenres.get(track.id());
        return List.of();
    }

    public boolean containsGenre(Genre genre){
        return genres.contains(genre);
    }
    public void addGenre(Genre genre){
        genres.add(genre);
    }

    public void addArtistGenres(Integer artistId, Genre genre){
        artistGenres.computeIfAbsent(artistId, _->new ArrayList<>());
        artistGenres.get(artistId).add(genre);
    }

    public void addAlbumGenres(Integer albumId, Genre genre){
        albumGenres.computeIfAbsent(albumId,_->new ArrayList<>());
        albumGenres.get(albumId).add(genre);
    }

    public void addTrackGenres(Integer trackId, Genre genre){
        trackGenres.computeIfAbsent(trackId,_->new ArrayList<>());
        trackGenres.get(trackId).add(genre);
    }


}
