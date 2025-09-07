package app.control.api;

public interface TrackSearch {
    String search(String token, String query) throws TooManyRequests;
}
