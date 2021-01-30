package BipartiteTopologyAPI.interfaces;

import BipartiteTopologyAPI.operations.RemoteCallIdentifier;
import BipartiteTopologyAPI.sites.NodeId;

import java.io.Serializable;

public interface Node extends Mergeable, Serializable {

    void init();

    void receiveQuery(long queryId, Serializable query);

    void receiveMsg(NodeId source, RemoteCallIdentifier rpc, Serializable message);

    void receiveTuple(Serializable tuple);

}
