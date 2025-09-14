package app.control.api;

public interface Search {
    String search(String token, String query) throws TooManyRequests;
}
