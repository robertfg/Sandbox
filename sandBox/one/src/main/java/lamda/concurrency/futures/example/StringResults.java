package lamda.concurrency.futures.example;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lamda.concurrency.futures.ConsolidatedResult;

/**
 * Result of Strings
 */
public class StringResults implements ConsolidatedResult<String> {

    private final List<String> results = new ArrayList<>();

    @Override
    public void addResult(final String result) {
        results.add(result);
    }

    public List<String> getResults() {
        return Collections.unmodifiableList(results);
    }
}
