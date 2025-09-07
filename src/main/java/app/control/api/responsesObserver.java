package app.control.api;

public interface responsesObserver {
    void notify(String query, String response);
    void finished();
}
