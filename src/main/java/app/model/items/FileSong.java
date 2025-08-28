package app.model.items;

public interface FileSong extends  FileReference {
    String title();
    String album();
    String artists();
    String request();
    String creation();
}
