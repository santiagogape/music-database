package app.control.api;

import app.control.TokenManager;
import app.model.utilities.Chronometer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatchTrackSearch {

    private final List<String> queries;
    private final TokenManager tokenManager;
    private final int delay;
    private final TrackSearch searcher;
    private final responsesObserver observer;
    private final ScheduledExecutorService scheduler;
    private final List<BatchObserver> batchObservers;

    public BatchTrackSearch(List<String> queries, TokenManager tokenManager, int delay, TrackSearch searcher, responsesObserver observer) {
        this.queries = queries;
        this.tokenManager = tokenManager;
        this.delay = delay;
        this.searcher = searcher;
        this.observer = observer;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.batchObservers = new ArrayList<>();
    }

    public interface BatchObserver{
        void start();
        void onFinish();
    }

    public void start() {
        int batchSize = 10;

        for (int i = 0; i < queries.size(); i += batchSize) {
            int end = Math.min(i + batchSize, queries.size());
            List<String> batch = queries.subList(i, end);
            System.out.println(i + " batch:" + batch);
            batchObservers.add(createBatchObserver(batch, i / batchSize, this.delay));
        }

        batchObservers.getFirst().start();

    }

    private BatchObserver createBatchObserver(List<String> batch, int index, int delay) {
        return new BatchObserver() {
            @Override
            public void start() {
                scheduler.schedule(() -> processBatch(batch, index), delay, TimeUnit.SECONDS);
            }

            @Override
            public void onFinish() {
                int next = index + 1;
                if (next == batchObservers.size()) {
                    observer.finished();
                    scheduler.shutdown();
                    System.out.println("BatchTrackSearch terminado.");
                    return;
                };
                batchObservers.get(next).start();
            }
        };
    }

    private void processBatch(List<String> batch, int index) {
        System.out.println("starting " + index + " batch");
        Chronometer chronometer = tokenManager.accessToken().getChronometer();
        System.out.printf("Time left - %02d:%02d%n", chronometer.getMinutes(), chronometer.getSeconds());
        for (String q : batch) {
            try {
                processQueryFromBatch(q);
            } catch (TooManyRequests e) {
                int retryAfter = Integer.parseInt(e.getMessage().split(":")[1]);
                System.out.println("429 recibido. Esperando " + retryAfter + "s...");
                scheduler.schedule(() -> processQueryFromBatch(q), retryAfter, TimeUnit.SECONDS);
            }
        }
        System.out.println("finished " + index + " batch");
        batchObservers.get(index).onFinish();
    }

    private void processQueryFromBatch(String q) throws TooManyRequests {
        System.out.println("next:");
        String response = searcher.search(tokenManager.accessToken().token(), q);
        System.out.println("notifying");
        observer.notify(q,response);
        Chronometer chronometer = tokenManager.accessToken().getChronometer();
        System.out.printf("Time left - %02d:%02d%n", chronometer.getMinutes(), chronometer.getSeconds());
    }
}
