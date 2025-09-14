package app.model.repositories;

import app.model.items.*;
import app.model.utilities.database.Database;


import java.util.*;

public class ItemsRepository {

    private final Map<Integer, Artist> artists;
    private final Map<Integer, Album> albums;
    private final Map<Integer, Track> tracks;
    private final Map<Integer, List<Integer>> artistsAlbums;
    private final Map<Integer, List<Integer>> albumsTracks;
    private final Map<Integer, List<Integer>> trackArtists;
    private final Map<Database.ItemSource, Map<SimpleItem.ItemType, List<SimpleItem.ItemUri>>> items;

    public ItemsRepository(Map<Integer, Artist> artists,
                           Map<Integer, Album> albums,
                           Map<Integer, Track> tracks,
                           Map<Integer, List<Integer>> artistsAlbums,
                           Map<Integer, List<Integer>> albumsTracks,
                           Map<Integer, List<Integer>> trackArtists,
                           Map<Database.ItemSource, Map<SimpleItem.ItemType, List<SimpleItem.ItemUri>>> items) {
        this.artists = artists;
        this.albums =  albums;
        this.tracks =  tracks;
        this.artistsAlbums = artistsAlbums;
        this.albumsTracks = albumsTracks;
        this.trackArtists = trackArtists;
        this.items = items;

    }

    public Optional<Artist> getArtistUriByItem(Database.ItemSource source, String sourceId){
        return getItemUriID(source, SimpleItem.ItemType.artist, sourceId).map(artists::get);
    }

    public Optional<Album> getAlbumUriByItem(Database.ItemSource source, String sourceId){
        return getItemUriID(source, SimpleItem.ItemType.album, sourceId).map(albums::get);
    }

    public Optional<Track> getTrackUriByItem(Database.ItemSource source, String sourceId){
        return getItemUriID(source, SimpleItem.ItemType.track, sourceId).map(tracks::get);
    }

    public Optional<Integer> getItemUriID(Database.ItemSource source, SimpleItem.ItemType type, String sourceId) {
        if (items.get(source) == null || items.get(source).get(type) == null) return Optional.empty();
        return items.get(source).get(type).stream()
                .filter(i->  i.sourceId().equals(sourceId))
                .map(SimpleItem.ItemUri::id)
                .findFirst();
    }


    public Map<Integer, Artist> getArtists() {
        return artists;
    }

    public Map<Integer, Album> getAlbums() {
        return albums;
    }

    public Map<Integer, Track> getTracks() {
        return tracks;
    }

    public Map<Integer, List<Integer>> getArtistsAlbums() {
        return artistsAlbums;
    }

    public Map<Integer, List<Integer>> getAlbumsTracks() {
        return albumsTracks;
    }

    public Map<Integer, List<Integer>> getTrackArtists() {
        return trackArtists;
    }

    public Map<Database.ItemSource, Map<SimpleItem.ItemType, List<SimpleItem.ItemUri>>> getItems() {
        return items;
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

    public List<Artist> trackArtists(Track track){
        return trackArtists.entrySet().stream()
                .filter(e -> e.getValue().contains(track.id()))
                .map(Map.Entry::getKey).map(artists::get).toList();
    }

    public List<Artist> albumArtists(Album album) {
        return artistsAlbums.entrySet().stream()
                .filter(e -> e.getValue().contains(album.id()))
                .map(Map.Entry::getKey).map(artists::get).toList();
    }
}
