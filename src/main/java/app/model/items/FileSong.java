package app.model.items;

public interface FileSong extends  FileReference {
    String title();
    String album();
    String artists();
    String request();
    record Individual(Integer id, String trackId){}
    record FileSongResponseIndividualTrackId(FileSong fileSong, Response response, String trackId){}
}
