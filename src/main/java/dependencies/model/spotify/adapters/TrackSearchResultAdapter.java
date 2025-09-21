package dependencies.model.spotify.adapters;

import app.model.items.Album;
import app.model.items.FileSong;
import app.model.items.Track;
import app.model.utilities.api.TrackSearchResultList;

import java.time.LocalDateTime;

public class TrackSearchResultAdapter implements Track {

    private final TrackSearchResultList.TrackSearchResult track;
    private final FileSong fileSong;
    private final Album album;

    public TrackSearchResultAdapter(TrackSearchResultList.TrackSearchResult track, FileSong fileSong, Album album) {
        this.track = track;
        this.fileSong = fileSong;
        this.album = album;
    }

    @Override
    public Integer albumId() {
        return album.id();
    }

    @Override
    public int number() {
        return track.trackNumber();
    }

    @Override
    public String directory() {
        return fileSong.directory();
    }

    @Override
    public LocalDateTime creation() {
        return fileSong.creation();
    }

    @Override
    public Integer id() {
        return 0;
    }

    @Override
    public String name() {
        return track.trackName();
    }

    @Override
    public String nameWithExtension() {
        return name()+".mp3";
    }

    @Override
    public ItemType type() {
        return ItemType.track;
    }
}
