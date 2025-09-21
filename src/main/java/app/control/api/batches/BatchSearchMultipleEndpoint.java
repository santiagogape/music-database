package app.control.api.batches;

import app.control.TokenManager;
import app.control.api.BatchObserver;
import app.control.api.ResponseObserver;
import app.control.api.TooManyRequests;
import app.model.utilities.Chronometer;
import app.model.utilities.api.EndpointMultipleSearchResultBasic;
import app.control.api.endpoints.MultipleEndpoint;
import app.model.utilities.api.Factory;
import app.model.utilities.api.factories.implementations.partial.EndpointSearchResultBasicPartialFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatchSearchMultipleEndpoint<
            S,
            T,
            R extends EndpointMultipleSearchResultBasic<T>,
            F extends EndpointSearchResultBasicPartialFactory<T> & Factory<R>
        > implements MultipleBatchSearcher<T,R> {

    private final TokenManager tokenManager;
    private final int delay;
    private final MultipleEndpoint<S, T,R,F> endpoint;
    private final ArrayList<BatchObserver> batchObservers;


    public BatchSearchMultipleEndpoint(
            TokenManager tokenManager,
            int delay,
            MultipleEndpoint<S, T,R,F> endpoint
    ) {
        this.tokenManager = tokenManager;
        this.delay = delay;
        this.endpoint = endpoint;
        this.batchObservers = new ArrayList<>();
    }

    @Override
    public void startNewBatchWith(List<String> ids, ResponseObserver<R> observer) {

        int searchLimit = endpoint.searchLimit();
        int batchSize = 10;

        List<List<String>> idGroupedBySearchLimit = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += searchLimit) {
            int end = Math.min(i + batchSize, ids.size());
            idGroupedBySearchLimit.add(ids.subList(i, end));
        }

        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            CountDownLatch onFinished = new CountDownLatch(1);
            for (int i = 0; i < idGroupedBySearchLimit.size(); i += batchSize) {
                int end = Math.min(i + batchSize, idGroupedBySearchLimit.size());
                List<List<String>> batch = idGroupedBySearchLimit.subList(i, end);
                System.out.println(i + " batch:" + batch);
                batchObservers.add(createBatchObserver(scheduler, onFinished, observer,batch, i / batchSize, this.delay));
            }
            batchObservers.getFirst().start();
            onFinished.await();
            System.out.println("batch artists finished");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private BatchObserver createBatchObserver(
            ScheduledExecutorService scheduler,
            CountDownLatch onFinished,
            ResponseObserver<R> observer,
            List<List<String>> batch,
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
                    observer.finish(onFinished);
                    scheduler.shutdown();
                    System.out.println("BatchSearch finished.");
                    return;
                }
                batchObservers.get(next).start();
            }
        };
    }

    private void processBatch(ScheduledExecutorService scheduler, ResponseObserver<R> observer, List<List<String>> batch, int index) {
        System.out.println("starting " + index + " batch");
        Chronometer chronometer = tokenManager.accessToken().getChronometer();
        System.out.printf("Time left - %02d:%02d%n", chronometer.getMinutes(), chronometer.getSeconds());
        for (List<String> ids : batch) {
            try {
                observer.notify(processQueryFromBatch(ids));
            } catch (TooManyRequests e) {
                int retryAfter = Integer.parseInt(e.getMessage().split(":")[1]);
                System.out.println("429 recibido. Esperando " + retryAfter + "s...");
                scheduler.schedule(() -> observer.notify(processQueryFromBatch(ids)), retryAfter, TimeUnit.SECONDS);

            }
        }
        batchObservers.get(index).onFinish();
    }

    private R processQueryFromBatch(List<String> ids) throws TooManyRequests {
        System.out.println("next:");
        R result = endpoint.search(tokenManager.accessToken().token(),ids);
        Chronometer chronometer = tokenManager.accessToken().getChronometer();
        System.out.printf("Time left - %02d:%02d%n", chronometer.getMinutes(), chronometer.getSeconds());
        return result;
    }

}
