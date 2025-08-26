package jaudiotagger;

public class Mp3Metadata {
    String fileName;
    String title;
    String album;
    String artists;

    public Mp3Metadata(String fileName, String title, String album, String artists) {
        this.fileName = fileName;
        this.title = title;
        this.album = album;
        this.artists = artists;
    }
}