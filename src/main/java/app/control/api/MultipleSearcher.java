package app.control.api;

import java.util.List;

public interface MultipleSearcher {
    /**
     * @param ids List<String> to be searched
     * @return String as response body
     */
    String search(String token, List<String> ids) throws TooManyRequests;
}
