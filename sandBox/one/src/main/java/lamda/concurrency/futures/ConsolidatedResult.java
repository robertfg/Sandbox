package lamda.concurrency.futures;
/**
 * Results of all the completed futures.
 * Created by brian on 4/26/14.
 */
public interface ConsolidatedResult<T> {

    void addResult(T result);
}
