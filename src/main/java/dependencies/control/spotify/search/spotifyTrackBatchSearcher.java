package dependencies.control.spotify.search;

import app.control.TokenManager;
import app.control.api.BatchObserver;
import app.control.api.ResponseObserver;
import app.control.api.TooManyRequests;
import app.control.api.batches.TrackBatchSearcher;
import app.model.utilities.Chronometer;
import app.model.utilities.api.TrackSearchEndpoint;
import app.model.utilities.api.TrackSearchResultList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class spotifyTrackBatchSearcher implements TrackBatchSearcher {

    private final TokenManager tokenManager;
    private final int delay;
    private final TrackSearchEndpoint searcher;
    private final ArrayList<BatchObserver> batchObservers;

    public spotifyTrackBatchSearcher(
            TokenManager tokenManager,
            int delay,
            TrackSearchEndpoint searcher) {
        this.tokenManager = tokenManager;
        this.delay = delay;
        this.searcher = searcher;
        this.batchObservers = new ArrayList<>();
    }

    @Override
    public void startNewBatchWith(List<String> queries, ResponseObserver<TrackSearchResultList> responseObserver) {
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            int batchSize = 10;
            CountDownLatch finisher = new CountDownLatch(1);
            for (int i = 0; i < queries.size(); i += batchSize) {
                int end = Math.min(i + batchSize, queries.size());
                List<String> batch = queries.subList(i, end);
                System.out.println(i + " batch:" + batch);
                batchObservers.add(
                        createBatchObserver(
                                scheduler, finisher, responseObserver,batch, i / batchSize, this.delay));
            }
            batchObservers.getFirst().start();
            finisher.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private BatchObserver createBatchObserver(
            ScheduledExecutorService scheduler,
            CountDownLatch finisher,
            ResponseObserver<TrackSearchResultList> observer,
            List<String> batch,
            int index,
            int delay) {
        return new BatchObserver() {
            @Override
            public void start() {
                scheduler.schedule(() -> processBatch(scheduler, observer,batch, index), delay, TimeUnit.SECONDS);
            }

            @Override
            public void onFinish() {
                int next = index + 1;
                if (next == batchObservers.size()) {
                    observer.finish(finisher);
                    System.out.println("BatchTrackSearch finished.");
                    return;
                }
                batchObservers.get(next).start();
            }
        };
    }

    private void processBatch(ScheduledExecutorService scheduler, ResponseObserver<TrackSearchResultList> observer, List<String> batch, int index) {
        System.out.println("starting " + index + " batch");
        Chronometer chronometer = tokenManager.accessToken().getChronometer();
        System.out.printf("Time left - %02d:%02d%n", chronometer.getMinutes(), chronometer.getSeconds());
        for (String q : batch) {
            try {
                observer.notify(processQueryFromBatch(q));
            } catch (TooManyRequests e) {
                int retryAfter = Integer.parseInt(e.getMessage().split(":")[1]);
                System.out.println("429 received. waiting " + retryAfter + "s...");
                scheduler.schedule(() -> observer.notify(processQueryFromBatch(q)), retryAfter, TimeUnit.SECONDS);
            }
        }
        System.out.println("finished " + index + " batch");
        batchObservers.get(index).onFinish();
    }

    private TrackSearchResultList processQueryFromBatch(String q) throws TooManyRequests {
        System.out.println("next:");
        TrackSearchResultList search = searcher.search(tokenManager.accessToken().token(), q);
        Chronometer chronometer = tokenManager.accessToken().getChronometer();
        System.out.printf("Time left - %02d:%02d%n", chronometer.getMinutes(), chronometer.getSeconds());
        return search;
    }
}
