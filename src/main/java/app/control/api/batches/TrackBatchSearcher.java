package app.control.api.batches;

import app.control.api.ResponseObserver;
import app.model.utilities.api.TrackSearchResultList;

import java.util.List;

public interface TrackBatchSearcher {
    void startNewBatchWith(List<String> queries, ResponseObserver<TrackSearchResultList> responsesObserver);
}
