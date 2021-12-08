package BipartiteTopologyAPI.interfaces;

import BipartiteTopologyAPI.operations.RemoteCallIdentifier;
import BipartiteTopologyAPI.sites.NodeId;

import java.io.Serializable;

/**
 * A basic interface of a Node in a Bipartite graph.
 */
public interface Node extends Mergeable, Serializable {

    /**
     * This method is called by the kernel upon the creation of the current Node instance.
     */
    void init();

    /**
     * A method called by the kernel to deliver a query to the implementation of the current node.
     *
     * @param queryId The id of the query.
     * @param query The Serializable query itself.
     */
    void receiveQuery(long queryId, Serializable query);

    /**
     * A method called by the kernel to deliver a message to the implementation of the current Node.
     *
     * @param source  The source of the message.
     * @param rpc     The Remote Procedure Call of the message.
     * @param message The Serializable message itself.
     */
    void receiveMsg(NodeId source, RemoteCallIdentifier rpc, Serializable message);

    /**
     * A method called by the kernel to deliver an item from the stream to the Node.
     *
     * @param tuple The record received from the data stream.
     */
    void receiveTuple(Serializable tuple);

}
