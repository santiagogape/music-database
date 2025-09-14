package app.control.api;

import app.control.TokenManager;
import app.model.utilities.Chronometer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatchArtistSearch implements BatchSearch{
    private final List<String> artistsId;
    private final TokenManager tokenManager;
    private final int delay;
    private final MultipleSearcher searcher;
    private final ResponsesObserver observer;
    private final ScheduledExecutorService scheduler;
    private final ArrayList<BatchObserver> batchObservers;

    public BatchArtistSearch(
            List<String> artistsId,
            TokenManager tokenManager,
            int delay,
            MultipleSearcher searcher,
            ResponsesObserver observer
    ) {
        this.artistsId = artistsId;
        this.tokenManager = tokenManager;
        this.delay = delay;
        this.searcher = searcher;
        this.observer = observer;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.batchObservers = new ArrayList<>();
    }

    @Override
    public void start() {
        int searchLimit = 50;
        int batchSize = 10;

        List<List<String>> severalArtists = new ArrayList<>();
        for (int i = 0; i < artistsId.size(); i += searchLimit) {
            int end = Math.min(i + batchSize, artistsId.size());
            severalArtists.add(artistsId.subList(i, end));
        }

        System.out.println("several artist size:"+severalArtists.size());
        for (int i = 0; i < severalArtists.size(); i += batchSize) {
            int end = Math.min(i + batchSize, severalArtists.size());
            List<List<String>> batch = severalArtists.subList(i, end);
            System.out.println(i + " batch:" + batch);
            batchObservers.add(createBatchObserver(batch, i / batchSize, this.delay));
        }
        System.out.println("batches:"+batchObservers.size());

        batchObservers.getFirst().start();
    }

    private BatchObserver createBatchObserver(List<List<String>> batch, int i, int delay) {
        return new BatchObserver() {
            @Override
            public void start() {
                scheduler.schedule(() -> processBatch(batch, i), delay, TimeUnit.SECONDS);
            }

            @Override
            public void onFinish() {
                int next = i + 1;
                if (next == batchObservers.size()) {
                    observer.finished();
                    scheduler.shutdown();
                    System.out.println("BatchTrackSearch terminado.");
                    return;
                }
                System.out.println("starting next batch:"+next);
                batchObservers.get(next).start();
            }
        };
    }

    private void processBatch(List<List<String>> batch, int index) {
        System.out.println("starting " + index + " batch");
        Chronometer chronometer = tokenManager.accessToken().getChronometer();
        System.out.printf("Time left - %02d:%02d%n", chronometer.getMinutes(), chronometer.getSeconds());
        for (List<String> ids: batch){
            try {
                processQueryFromBatch(ids);
            } catch (TooManyRequests e) {
                int retryAfter = Integer.parseInt(e.getMessage().split(":")[1]);
                System.out.println("429 recibido. Esperando " + retryAfter + "s...");
                scheduler.schedule(() -> processQueryFromBatch(ids), retryAfter, TimeUnit.SECONDS);
            }
        }
        System.out.println("finished " + index + " batch");
        batchObservers.get(index).onFinish();
    }

    private void processQueryFromBatch(List<String> ids)  throws TooManyRequests {
        System.out.println("next:");
        String response = searcher.search(tokenManager.accessToken().token(), ids);
        System.out.println("notifying");
        observer.notify("ids:"+String.join(",",ids),response);
        Chronometer chronometer = tokenManager.accessToken().getChronometer();
        System.out.printf("Time left - %02d:%02d%n", chronometer.getMinutes(), chronometer.getSeconds());
    }
}
