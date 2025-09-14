package app.control.api;

public interface BatchObserver {
    void start();

    void onFinish();
}
