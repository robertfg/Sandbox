package locks;

import java.util.concurrent.Callable;

public interface ITediousTask<T> extends Callable<T> {

}
