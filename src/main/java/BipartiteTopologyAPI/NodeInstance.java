package BipartiteTopologyAPI;

import BipartiteTopologyAPI.annotations.Inject;
import BipartiteTopologyAPI.futures.BroadcastValueResponse;
import BipartiteTopologyAPI.futures.BroadcastValuesResponses;
import BipartiteTopologyAPI.futures.PromiseResponse;
import BipartiteTopologyAPI.futures.PromisedResponses;
import BipartiteTopologyAPI.interfaces.Network;
import BipartiteTopologyAPI.operations.CallType;
import BipartiteTopologyAPI.operations.RemoteCallIdentifier;
import BipartiteTopologyAPI.sites.NodeId;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract class of a node in the Bipartite Network. This class provides immutable information of the Network to the
 * user, like the proxies of the disjoint remote nodes of the Bipartite Network graph, the proxy to an external user
 * that can pose queries to this node, a map of promises made by this node to remote disjoint nodes and more basic
 * information like the id of the network, the id of the node and the number of hubs and spokes.
 *
 * @param <ProxyIfc> The interface for the proxy of the disjoint remote nodes.
 * @param <QueryIfc> The interface of the querier.
 */
public abstract class NodeInstance<ProxyIfc, QueryIfc> {

    @Inject
    private NetworkContext<ProxyIfc, QueryIfc> networkContext;

    @Inject
    private GenericWrapper genericWrapper;

    private boolean broadcasted = false;

    /**
     * A private method that uses Java Reflection in order to set up a promise.
     *
     * @param promise The promise to be set up.
     */
    private <T extends Serializable> void setPromise(PromiseResponse<T> promise) {
        try {
            assert genericWrapper.currentRPC.getCallType().equals(CallType.TWO_WAY) :
                    "No promise can be made, as " + ((genericWrapper.nodeId.isHub()) ? "Spoke " : "Hub ") +
                            getCurrentCaller() + " of network " + getNetworkID() +
                            " does not wait for any answer from " + genericWrapper.nodeId;
            Field rpcField = promise.getClass().getDeclaredField("rpc");
            rpcField.setAccessible(true);
            rpcField.set(promise, new RemoteCallIdentifier(genericWrapper.currentRPC.getCallNumber()));
            Field networkField = promise.getClass().getDeclaredField("network");
            networkField.setAccessible(true);
            networkField.set(promise, genericWrapper.network);
            Field sourceField = promise.getClass().getDeclaredField("source");
            sourceField.setAccessible(true);
            sourceField.set(promise, genericWrapper.nodeId);
            Field destinationField = promise.getClass().getDeclaredField("destination");
            destinationField.setAccessible(true);
            destinationField.set(promise, genericWrapper.currentCaller);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method used by the user when it makes a promise to a disjoint remote node of the Bipartite Network graph. This
     * should be used when the current node does not posses the answer to a question of a disjoint remote node, but will
     * arrive (or produced) at a later time. When the answer(s) is (are) available, then {@link #fulfillPromises(List)}
     * should be called by the user to send back the answer.
     *
     * @return The pair with the id of the disjoint node that invoked this node instance along with the promise id that
     * it was made to it.
     */
    public <T extends Serializable> PromiseResponse<T> makePromise() {
        PromiseResponse<T> promise = new PromiseResponse<>();
        setPromise(promise);
        int promiseId = 0;
        if (!networkContext.promises.containsKey(getCurrentCaller()))
            networkContext.promises.put(getCurrentCaller(), new HashMap<>());
        else
            promiseId = networkContext.promises.get(getCurrentCaller()).size() + 1;
        networkContext.promises.get(getCurrentCaller()).put(promiseId, promise);
        return promise;
    }

    /**
     * This method fulfills the promises given by the current node to the disjoint remote node of the Bipartite Network.
     *
     * @param answers The answers to the promises made to the disjoint remote node of the Bipartite Network.
     */
    public <T extends Serializable> PromisedResponses<T> fulfillPromises(int nodeId, List<T> answers)
            throws NoSuchFieldException, IllegalAccessException {
        if (networkContext.promises.containsKey(nodeId)) {
            HashMap<Integer, PromiseResponse> promises = (HashMap<Integer, PromiseResponse>) networkContext.promises.get(nodeId).clone();
            assert (answers.size() == promises.size());
            PromisedResponses<T> responses = new PromisedResponses<>();
            Field prField = responses.getClass().getDeclaredField("promisedResponses");
            prField.setAccessible(true);
            prField.set(responses, new ArrayList<>(promises.values()));
            Field anField = responses.getClass().getDeclaredField("answers");
            anField.setAccessible(true);
            anField.set(responses, answers);
            networkContext.promises.get(nodeId).clear();
            return responses;
        } else {
            throw new RuntimeException("The " +
                    ((genericWrapper.nodeId.isHub()) ? "Hub " : "Spoke ") + getNodeId() + " of network " +
                    getNetworkID() + " has not made any promises to " +
                    ((genericWrapper.nodeId.isHub()) ? "Spoke " : "Hub ") + nodeId + ".");
        }
    }

    public <T extends Serializable> PromisedResponses<T> fulfillPromises(List<T> answers)
            throws NoSuchFieldException, IllegalAccessException {
        return fulfillPromises(getCurrentCaller(), answers);
    }

    /**
     * This method fulfills a promise given by the current node to α disjoint remote node of the Bipartite Network.
     *
     * @param destination The id of the node and the promise to be fulfilled.
     * @param answer      The answer to the promise made to the disjoint remote node of the Bipartite Network.
     */
    public void fulfillPromise(Pair<Integer, Integer> destination, Serializable[] answer) {
        if (networkContext.promises.containsKey(destination.getKey())) {
            HashMap<Integer, PromiseResponse> promises = networkContext.promises.get(destination.getKey());
            if (promises.containsKey(destination.getValue())) {
                promises.get(destination.getValue()).sendAnswer(answer);
            } else {
                throw new RuntimeException("The " +
                        ((genericWrapper.nodeId.isHub()) ? "Hub " : "Spoke ") + getNodeId() + " of network " +
                        getNetworkID() + " has not made a promise with id " + destination.getValue() + " to caller " +
                        ((genericWrapper.nodeId.isHub()) ? "Spoke " : "Hub ") + destination.getKey() + ".");
            }
        } else {
            throw new RuntimeException("The " +
                    ((genericWrapper.nodeId.isHub()) ? "Hub " : "Spoke ") + getNodeId() + " of network " +
                    getNetworkID() + " has not made a promise to " +
                    ((genericWrapper.nodeId.isHub()) ? "Spoke " : "Hub ") + destination.getKey() + ".");
        }
    }

    public void fulfillPromise(int promiseId, Serializable[] answer) {
        fulfillPromise(Pair.of(getCurrentCaller(), promiseId), answer);
    }

    /**
     * A method used by the user when it makes a broadcast promise to a disjoint remote node of the Bipartite Network.
     * This should be used when the answer for a specific node is the same for all the disjoint nodes, but do not posses
     * it yet. When the answer is available, {@link #fulfillBroadcastPromises(List)} should be called to broadcast the
     * answer to all the disjoint nodes.
     */
    public <T extends Serializable> PromiseResponse<T> makeBroadcastPromise() {
        if (broadcasted) {
            for (HashMap<Integer, PromiseResponse> bPromises : networkContext.broadcastPromises.values())
                bPromises.clear();
            broadcasted = false;
        }
        PromiseResponse<T> promise = new PromiseResponse<>();
        setPromise(promise);
        int promiseId = 0;
        if (!networkContext.broadcastPromises.containsKey(getCurrentCaller()))
            networkContext.broadcastPromises.put(getCurrentCaller(), new HashMap<>());
        else
            promiseId = networkContext.broadcastPromises.get(getCurrentCaller()).size() + 1;
        networkContext.broadcastPromises.get(getCurrentCaller()).put(promiseId, promise);
        return promise;
    }

    /**
     * The method to call upon the arrival of an answer that needs to be broadcasted to all the disjoint nodes of the
     * Bipartite Network.
     *
     * @param answer The answer to the broadcast promise.
     */
    public <T extends Serializable> BroadcastValuesResponses<T> fulfillBroadcastPromises(List<T> answer) {
        if (networkContext.broadcastPromises.isEmpty()) {
            throw new RuntimeException("No broadcast promises made to fulfill.");
        } else {
            try {
                // Make the private fields accessible.
                PromiseResponse r = networkContext.broadcastPromises
                        .entrySet().iterator().next().getValue()
                        .entrySet().iterator().next().getValue();
                Field networkField = r.getClass().getDeclaredField("network");
                networkField.setAccessible(true);
                Field sourceField = r.getClass().getDeclaredField("source");
                sourceField.setAccessible(true);
                Field destField = r.getClass().getDeclaredField("destination");
                destField.setAccessible(true);
                Field rpcField = r.getClass().getDeclaredField("rpc");
                rpcField.setAccessible(true);

                // Get the private fields.
                Network net = (Network) networkField.get(r);
                NodeId src = (NodeId) sourceField.get(r);


                ArrayList<ArrayList<PromiseResponse>> pr = new ArrayList<>();
                for (HashMap<Integer, PromiseResponse> entry : networkContext.broadcastPromises.values())
                    pr.add(new ArrayList<>(entry.values()));
                ArrayList<BroadcastValueResponse<T>> bResponses = new ArrayList<>();
                for (int i = 0; i < pr.get(0).size(); i++) {
                    Map<NodeId, RemoteCallIdentifier> rpcs = new HashMap<>();
                    for (ArrayList<PromiseResponse> promiseResponses : pr) {
                        PromiseResponse p = promiseResponses.get(i);
                        rpcs.put((NodeId) destField.get(p), (RemoteCallIdentifier) rpcField.get(p));
                    }
                    bResponses.add(new BroadcastValueResponse<>(net, src, rpcs, answer.get(i)));
                }

                broadcasted = true;
                return new BroadcastValuesResponses<>(bResponses);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    // ============================================ Public Getters =====================================================

    public int getNetworkID() {
        return networkContext.networkId;
    }

    public int getNodeId() {
        return networkContext.nodeId;
    }

    public int getNumberOfHubs() {
        return networkContext.numberOfHubs;
    }

    public int getNumberOfSpokes() {
        return networkContext.numberOfSpokes;
    }

    public ProxyIfc getProxy(Integer proxyId) {
        return networkContext.proxies.get(proxyId);
    }

    public ProxyIfc getBroadcastProxy() {
        return networkContext.broadcastProxy;
    }

    public QueryIfc getQuerier() {
        return networkContext.querier;
    }

    public int getCurrentCaller() {
        return genericWrapper.currentCaller.getNodeId();
    }

    public boolean isBlocked() {
        return genericWrapper.isBlocked();
    }

    public void blockStream() {
        genericWrapper.block();
    }

    public void unblockStream() {
        genericWrapper.unblock();
    }

}
