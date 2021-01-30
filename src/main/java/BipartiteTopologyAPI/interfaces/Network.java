package BipartiteTopologyAPI.interfaces;

import BipartiteTopologyAPI.sites.NodeId;
import BipartiteTopologyAPI.operations.RemoteCallIdentifier;
import BipartiteTopologyAPI.sites.NetworkDescriptor;

import java.io.Serializable;
import java.util.Map;

public interface Network extends Serializable {

    void send(NodeId source, NodeId destination, RemoteCallIdentifier rpc, Serializable message);

    void broadcast(NodeId source, Map<NodeId, RemoteCallIdentifier> rpcMap, Serializable message);

    NetworkDescriptor describe();

}
