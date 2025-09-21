package app.model.utilities.api.factories;

import app.model.utilities.api.EndpointMultipleSearchResultBasic;
import app.model.utilities.api.Factory;
import app.model.utilities.api.factories.implementations.partial.EndpointSearchResultBasicPartialFactory;

public interface FactoryFiller<S,T, R extends EndpointMultipleSearchResultBasic<T>, F extends EndpointSearchResultBasicPartialFactory<T> & Factory<R>> {
    void fill(S source,F factory );
}
