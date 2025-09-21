package app.control.api.endpoints;

import app.control.api.TooManyRequests;
import app.control.files.json.JsonConverter;
import app.model.utilities.api.EndpointMultipleSearchResultBasic;
import app.model.utilities.api.EndpointRequest;
import app.model.utilities.api.Factory;
import app.model.utilities.api.factories.FactoryFiller;
import app.model.utilities.api.factories.implementations.partial.EndpointSearchResultBasicPartialFactory;

import java.util.List;

import static app.control.api.endpoints.IDsFormater.urlFromIds;


public class MultipleEndpoint<K,T, R extends EndpointMultipleSearchResultBasic<T>, F extends EndpointSearchResultBasicPartialFactory<T> & Factory<R>>  {

    private final JsonConverter converter;
    private final F factory;
    private final FactoryFiller<K,T,R,F> factoryFiller;
    private final String endpoint;
    private final Class<K> sourceSerializedType;
    private final EndpointRequest endpointRequest;
    private final int maxAmountOfIds;

    public MultipleEndpoint(
            JsonConverter converter,
            EndpointRequest endpointRequest,
            int maxAmountOfIds,
            F factory,
            FactoryFiller<K,T,R,F> factoryFiller,
            String endpoint,
            Class<K> sourceSerializedType
    ) {
        this.converter = converter;
        this.factory = factory;
        this.factoryFiller = factoryFiller;
        this.endpoint = endpoint;
        this.endpointRequest = endpointRequest;
        this.maxAmountOfIds = maxAmountOfIds;
        this.sourceSerializedType = sourceSerializedType;
    }

    public R search(String token, List<String> ids) throws TooManyRequests {
        String body = endpointRequest.requestAndGetResponseBody(token, urlFromIds(endpoint,ids));
        factoryFiller.fill(converter.fromJson(body,sourceSerializedType), factory);
        return factory.get();
    }


    public int searchLimit() {
        return maxAmountOfIds;
    }

}
