package app.control.api;

import java.util.concurrent.CountDownLatch;

public interface ResponseObserver<T> {
    void notify(T result);
    void finish(CountDownLatch finisher);
}
