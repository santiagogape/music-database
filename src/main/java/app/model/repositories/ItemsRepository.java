package app.model.repositories;

import app.model.items.*;
import app.model.utilities.database.Database;


import java.util.*;
import java.util.stream.Collectors;

public class ItemsRepository {

    private final Map<Integer, Artist> artists;
    private final Map<Integer, Album> albums;
    private final Map<Integer, Track> tracks;
    private final Map<Integer, List<Integer>> artistsAlbums;
    private final Map<Integer, List<Integer>> albumsTracks;
    private final Map<Integer, List<Integer>> trackArtists;
    private final Map<Database.ItemSource, Map<SimpleItem.ItemType, Map<String, SimpleItem.ItemUri>>> items;

    public ItemsRepository(Map<Integer, Artist> artists,
                           Map<Integer, Album> albums,
                           Map<Integer, Track> tracks,
                           Map<Integer, List<Integer>> artistsAlbums,
                           Map<Integer, List<Integer>> albumsTracks,
                           Map<Integer, List<Integer>> trackArtists,
                           Map<Database.ItemSource, Map<SimpleItem.ItemType, Map<String, SimpleItem.ItemUri>>> items) {
        this.artists = artists;
        this.albums =  albums;
        this.tracks =  tracks;
        this.artistsAlbums = artistsAlbums;
        this.albumsTracks = albumsTracks;
        this.trackArtists = trackArtists;
        this.items = items;
        artists.keySet().forEach(k->artistsAlbums.putIfAbsent(k,new ArrayList<>()));
        albums.keySet().forEach(k->albumsTracks.putIfAbsent(k,new ArrayList<>()));
        tracks.keySet().forEach(k->trackArtists.putIfAbsent(k,new ArrayList<>()));

    }

    public Optional<Artist> getArtistUriByItem(Database.ItemSource source, String sourceId){
        return getItemUriID(source, SimpleItem.ItemType.artist, sourceId).map(artists::get);
    }

    public Optional<Album> getAlbumUriByItem(Database.ItemSource source, String sourceId){
        return getItemUriID(source, SimpleItem.ItemType.album, sourceId).map(albums::get);
    }

    public Optional<Integer> getItemUriID(Database.ItemSource source, SimpleItem.ItemType type, String sourceId) {
        if (items.get(source) == null ||
                items.get(source).get(type) == null ||
                !items.get(source).get(type).containsKey(sourceId)
        ) return Optional.empty();
        return Optional.of(items.get(source).get(type).get(sourceId).id());
    }

    public Map<String, Artist> getArtistsFrom(Database.ItemSource source){
        if (items.containsKey(source)) return items.get(source).get(SimpleItem.ItemType.artist).values().stream().collect(
                Collectors.toMap(SimpleItem.ItemUri::sourceId, i->artists.get(i.id()))
        );
        else return Map.of();
    }

    public List<Album> getArtistAlbums(Artist artist){
        return artistsAlbums.get(artist.id()).stream().map(albums::get).toList();
    }

    public void addArtistsAlbum(Integer artistId, Integer albumId){
        System.out.println("adding "+artistId+","+albumId);
        System.out.println(artistsAlbums.get(artistId));
        artistsAlbums.get(artistId).add(albumId);
        System.out.println(artistsAlbums.get(artistId));
    }

    public void addAlbumTrack(Integer albumId, Integer trackId){
        albumsTracks.get(albumId).add(trackId);
    }

    public void addTrackArtist(Integer trackId, Integer artistId){
        trackArtists.get(trackId).add(artistId);
    }

    public void addTrack(Track track){
        if (!tracks.containsKey(track.id())) {
            tracks.put(track.id(),track);
            trackArtists.put(track.id(),new ArrayList<>());
        }
    }

    public void addAlbum(Album album){
        if (!albums.containsKey(album.id())) {
            albums.put(album.id(),album);
            albumsTracks.put(album.id(),new ArrayList<>());
        }
    }

    public void addArtist(Artist artist){
        if (!artists.containsKey(artist.id())) {
            artists.put(artist.id(),artist);
            artistsAlbums.put(artist.id(),new ArrayList<>());
        }
    }

    public void addItemUri(SimpleItem.ItemUri itemUri){
        items.computeIfAbsent(itemUri.source(), _ -> new HashMap<>())
                .computeIfAbsent(itemUri.type(),_ -> new HashMap<>())
                .put(itemUri.sourceId(),itemUri);
    }
}
