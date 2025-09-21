package app.control.api.batches;

import app.control.api.ResponseObserver;
import app.model.utilities.api.EndpointMultipleSearchResultBasic;

import java.util.List;


public interface MultipleBatchSearcher<T, R extends EndpointMultipleSearchResultBasic<T>> {
    void startNewBatchWith(List<String> ids, ResponseObserver<R> observer);
}
