package BipartiteTopologyAPI.futures;

import BipartiteTopologyAPI.network.Network;
import BipartiteTopologyAPI.operations.RemoteCallIdentifier;
import BipartiteTopologyAPI.sites.NodeId;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

public class BroadcastValueResponse<T extends Serializable> implements Response<T> {

    /**
     * The network to send the response to.
     */
    private final Network network;

    /**
     * The source of the response.
     */
    private final NodeId source;

    /**
     * The destination and operation pairs of the response.
     */
    private final Map<NodeId, RemoteCallIdentifier> operations;

    /**
     * The actual Serializable broadcast response value.
     */
    protected T value;

    public BroadcastValueResponse(Network network,
                                  NodeId source,
                                  Map<NodeId, RemoteCallIdentifier> operations,
                                  T value) {
        this.network = network;
        this.source = source;
        this.operations = operations;
        this.value = value;
    }

    @Override
    public T getValue() {
        throw new UnsupportedOperationException("getValue() called on BroadcastValueResponse");
    }

    @Override
    public void to(Consumer<T> consumer) {
        throw new UnsupportedOperationException("to() called on BroadcastValueResponse");
    }

    @Override
    public void toSync(Consumer<T> consumer) {
        throw new UnsupportedOperationException("toSync() called on BroadcastValueResponse");
    }

    /**
     * This method fulfills the broadcast promise by sending the provided answer to the  all the disjoint
     * remote node callers.
     */
    public void broadcastResponse() {
        network.broadcast(source, operations, value);
    }

}
