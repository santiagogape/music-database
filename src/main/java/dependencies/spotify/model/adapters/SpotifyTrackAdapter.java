package dependencies.spotify.model.adapters;

import app.model.items.Album;
import app.model.items.FileSong;
import app.model.items.Track;
import dependencies.spotify.model.items.full.SpotifyTrack;

import java.time.LocalDateTime;

public class SpotifyTrackAdapter implements Track {

    private final SpotifyTrack track;
    private final FileSong fileSong;
    private final Album album;

    public SpotifyTrackAdapter(SpotifyTrack track, FileSong fileSong, Album album) {
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
        return track.getTrackNumber();
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
        return track.getName();
    }

    @Override
    public ItemType type() {
        return ItemType.track;
    }
}
