package BipartiteTopologyAPI;

import BipartiteTopologyAPI.futures.PromiseResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A final class containing all the information of a node connected to the Bipartite Network. This class is the holder
 * of all the proxies for the disjoint remote nodes.
 *
 * @param <ProxyIfc> The interface for the proxy of the disjoint remote nodes.
 * @param <QueryIfc> The interface of the querier.
 */
public final class NetworkContext<ProxyIfc, QueryIfc> implements Serializable {

    /**
     * The id of the Bipartite Network.
     */
    public final int networkId;

    /**
     * The id of the current node.
     */
    public final int nodeId;

    /**
     * The number of hubs of the Bipartite Network.
     */
    public final int numberOfHubs;

    /**
     * The number of spokes of the Bipartite Network.
     */
    public final int numberOfSpokes;

    /**
     * This is the proxy to the querier. This proxy is used to answer queries to the querier.
     */
    public final QueryIfc querier;

    /**
     * Proxies for the disjoint nodes of the Bipartite Graph.
     */
    public final Map<Integer, ProxyIfc> proxies;

    /**
     * A proxy that broadcasts to all disjoint nodes of the Bipartite Graph.
     */
    public final ProxyIfc broadcastProxy;

    /**
     * A map of promises that this node has made to the disjoint nodes of the Bipartite Graph.
     */
    public final HashMap<Integer, HashMap<Long, PromiseResponse>> promises;

    /**
     * A map of promises that this node has made to the disjoint nodes of the Bipartite Graph.
     */
    public final HashMap<Integer, HashMap<Long, PromiseResponse>> broadcastPromises;

    private NetworkContext(int networkId,
                           int numberOfHubs,
                           int numberOfSpokes,
                           int nodeId,
                           QueryIfc querier,
                           Map<Integer, ProxyIfc> proxies,
                           ProxyIfc broadcastProxy,
                           HashMap<Integer, HashMap<Long, PromiseResponse>> promises,
                           HashMap<Integer, HashMap<Long, PromiseResponse>> broadcastPromises) {
        this.networkId = networkId;
        this.numberOfHubs = numberOfHubs;
        this.numberOfSpokes = numberOfSpokes;
        this.nodeId = nodeId;
        this.querier = querier;
        this.proxies = proxies;
        this.broadcastProxy = broadcastProxy;
        this.promises = promises;
        this.broadcastPromises = broadcastPromises;
    }

    static public <PrIfc, QuIfc> NetworkContext<PrIfc, QuIfc> forNode(int networkId,
                                                                      int numberOfHubs,
                                                                      int numberOfSpokes,
                                                                      int nodeId,
                                                                      QuIfc querier,
                                                                      Map<Integer, PrIfc> proxies,
                                                                      PrIfc broadcastProxy) {
        return new NetworkContext<>(networkId,
                numberOfHubs,
                numberOfSpokes,
                nodeId,
                querier,
                proxies,
                broadcastProxy,
                new HashMap<>(),
                new HashMap<>());
    }

    static public <PrIfc, QuIfc> NetworkContext<PrIfc, QuIfc> forNode(int networkId,
                                                                      int numberOfHubs,
                                                                      int numberOfSpokes,
                                                                      int nodeId,
                                                                      QuIfc querier,
                                                                      Map<Integer, PrIfc> proxies,
                                                                      PrIfc broadcastProxy,
                                                                      HashMap<Integer, HashMap<Long, PromiseResponse>> promises,
                                                                      HashMap<Integer, HashMap<Long, PromiseResponse>> broadcastPromises) {
        return new NetworkContext<>(networkId,
                numberOfHubs,
                numberOfSpokes,
                nodeId,
                querier,
                proxies,
                broadcastProxy,
                promises,
                broadcastPromises);
    }

}
