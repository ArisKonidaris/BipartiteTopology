package BipartiteTopologyAPI.futures;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

public class BroadcastValuesResponses<T extends Serializable> implements Response<T> {

    private final List<BroadcastValueResponse<T>> broadcasts;

    public BroadcastValuesResponses(List<BroadcastValueResponse<T>> broadcasts) {
        this.broadcasts = broadcasts;
    }

    @Override
    public void to(Consumer<T> consumer) {
        throw new UnsupportedOperationException("to() called on BroadcastValuesResponses");
    }

    @Override
    public void toSync(Consumer<T> consumer) {
        throw new UnsupportedOperationException("toSync() called on BroadcastValuesResponses");
    }

    @Override
    public T getValue() {
        throw new UnsupportedOperationException("getValue() called on BroadcastValuesResponses");
    }

    /**
     * This method fulfills the broadcast promise by sending the provided answer to the  all the disjoint
     * remote node callers.
     */
    public void broadcastResponse() {
        for (BroadcastValueResponse<T> broadcast : broadcasts)
            broadcast.broadcastResponse();
    }

}
