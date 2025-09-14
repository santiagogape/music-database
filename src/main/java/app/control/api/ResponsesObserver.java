package app.control.api;

public interface ResponsesObserver {
    void notify(String query, String responseBody);
    void finished();
}
