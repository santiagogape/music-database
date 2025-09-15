package app.control.items;

import app.model.items.Album;
import app.model.items.Artist;
import app.model.items.Track;

import java.time.LocalDateTime;

public class ItemIDUpdater {

    public ItemIDUpdater() {}

    public Artist update(Integer id, Artist artist){
        return new Artist() {
            @Override
            public Integer id() {
                return id;
            }

            @Override
            public String name() {
                return artist.name();
            }

            @Override
            public ItemType type() {
                return ItemType.artist;
            }
        };
    }

    public Album update(Integer id, Album album){
        return new Album() {
            @Override
            public AlbumType albumType() {
                return album.albumType();
            }

            @Override
            public int tracks() {
                return album.tracks();
            }

            @Override
            public LocalDateTime release() {
                return album.release();
            }

            @Override
            public ReleasePrecision precision() {
                return album.precision();
            }

            @Override
            public String label() {
                return album.label();
            }

            @Override
            public Integer id() {
                return id;
            }

            @Override
            public String name() {
                return album.name();
            }

            @Override
            public ItemType type() {
                return ItemType.album;
            }
        };
    }

    public Track update(Integer id, Track track){
        return new Track() {
            @Override
            public Integer albumId() {
                return track.albumId();
            }

            @Override
            public int number() {
                return track.number();
            }

            @Override
            public String directory() {
                return track.directory();
            }

            @Override
            public LocalDateTime creation() {
                return track.creation();
            }

            @Override
            public Integer id() {
                return id;
            }

            @Override
            public String name() {
                return track.name();
            }

            @Override
            public String nameWithExtension() {
                return track.nameWithExtension();
            }

            @Override
            public ItemType type() {
                return ItemType.track;
            }
        };

    }


}
