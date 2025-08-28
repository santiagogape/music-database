package app.model.repositories;

import app.model.items.Album;
import app.model.items.Artist;
import app.model.items.FileReference;
import app.model.items.Track;


import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class itemsRepository {

    private final Map<Integer, Artist> artists;
    private final Map<Integer, Album> albums;
    private final Map<Integer, Track> tracks;
    private final Map<Integer, List<Integer>> artistsAlbums;
    private final Map<Integer, List<Integer>> albumsTracks;
    private final Map<Integer, List<Integer>> trackArtists;

    public itemsRepository(List<Artist> artists, List<Album> albums, List<Track> tracks, Map<Integer, List<Integer>> artistsAlbums, Map<Integer, List<Integer>> albumsTracks, Map<Integer, List<Integer>> trackArtists) {
        this.artists = artists.stream().collect(Collectors.toMap(FileReference::id, a -> a));
        this.albums = albums.stream().collect(Collectors.toMap(FileReference::id, a -> a));
        this.tracks = tracks.stream().collect(Collectors.toMap(FileReference::id, a -> a));
        this.artistsAlbums = artistsAlbums;
        this.albumsTracks = albumsTracks;
        this.trackArtists = trackArtists;
    }

    public List<Artist> getArtists() {
        return artists.values().stream().toList();
    }

    public List<Album> getAlbums() {
        return albums.values().stream().toList();
    }

    public List<Track> getTracks() {
        return tracks.values().stream().toList();
    }

    public List<Album> artistAlbums(Artist artist){
        return artistsAlbums.get(artist.id()).stream().map(albums::get).toList();
    }

    public List<Track> albumTracks(Album album){
        return albumsTracks.get(album.id()).stream().map(tracks::get).toList();
    }

    public Album trackAlbum(Track track){
        return albumsTracks.entrySet().stream()
                .filter(e -> e.getValue().contains(track.id()))
                .map(Map.Entry::getKey).map(albums::get).findFirst()
                .orElseThrow(() -> new NoSuchElementException("no album"));
    }

    public List<Artist> albumArtists(Album album) {
        return artistsAlbums.entrySet().stream()
                .filter(e -> e.getValue().contains(album.id()))
                .map(Map.Entry::getKey).map(artists::get).toList();
    }

    public List<Artist> trackArtists(Track track){
        return trackArtists.entrySet().stream()
                .filter(e -> e.getValue().contains(track.id()))
                .map(Map.Entry::getKey).map(artists::get).toList();
    }
}
