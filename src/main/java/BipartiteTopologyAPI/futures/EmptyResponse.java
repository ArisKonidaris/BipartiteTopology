package BipartiteTopologyAPI.futures;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * This class represents an empty response to a remote procedure call.
 */
public class EmptyResponse<T extends Serializable> implements Response<T> {

    @Override
    public void to(Consumer<T> consumer) {
        throw new RuntimeException("Illegal operation on ValueResponse");
    }

    @Override
    public void toSync(Consumer<T> consumer) {
        throw new RuntimeException("Illegal operation on ValueResponse");
    }

    @Override
    public T getValue() {
        throw new RuntimeException("Illegal operation on ValueResponse");
    }
}
