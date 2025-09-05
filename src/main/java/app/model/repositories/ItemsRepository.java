package app.model.repositories;

import app.model.items.*;


import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ItemsRepository {

    private final Map<Integer, Artist> artists;
    private final Map<Integer, Album> albums;
    private final Map<Integer, Track> tracks;
    private final Map<Integer, List<Integer>> artistsAlbums;
    private final Map<Integer, List<Integer>> albumsTracks;
    private final Map<Integer, List<Integer>> trackArtists;

    public ItemsRepository(List<Artist> artists, List<Album> albums, List<Track> tracks, Map<Integer, List<Integer>> artistsAlbums, Map<Integer, List<Integer>> albumsTracks, Map<Integer, List<Integer>> trackArtists) {
        this.artists = artists.stream().collect(Collectors.toMap(SimpleItem::id, a -> a));
        this.albums = albums.stream().collect(Collectors.toMap(SimpleItem::id, a -> a));
        this.tracks = tracks.stream().collect(Collectors.toMap(SimpleItem::id, a -> a));
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

    public void addArtist(Artist artist){
        artists.put(artist.id(), artist);
    }

    public void addAlbum(Album album){
        albums.put(album.id(), album);
    }

    public void addTrack(Track track){
        tracks.put(track.id(), track);
    }

    public void addAlbumArtists(Integer albumId, List<Integer> artistsIds){
        if (albums.containsKey(albumId)) {
            artistsIds.forEach(artist -> {
                if (artists.containsKey(artist)) artistsAlbums.get(artist).add(albumId);
            });
        }
    }

    public void addAlbumTracks(Integer albumId, List<Integer> trackIds){
        if (albums.containsKey(albumId)) {
            trackIds.forEach(track -> {
                if (tracks.containsKey(track)) albumsTracks.get(albumId).add(track);
            });
        }
    }

    public void addTrackArtists(Integer trackId, List<Integer> artistsIds){
        if (tracks.containsKey(trackId)) {
            artistsIds.forEach(artist -> {
                if (artists.containsKey(artist)) trackArtists.get(trackId).add(artist);
            });
        }
    }
}
