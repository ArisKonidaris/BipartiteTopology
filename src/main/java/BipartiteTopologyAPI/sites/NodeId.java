package BipartiteTopologyAPI.sites;

import java.io.Serializable;

/**
 * This is a basic immutable Java class that implements a unique identifier
 * of a node in a Bipartite Network. The NodeType determines
 * the type of the node (hub or spoke), and the nodeId integer is the
 * non-negative id of the node.
 * <p>
 * nodeType: This is the type of node in the Bipartite Network.
 * nodeId: This should always be a non-negative value.
 */
public class NodeId implements Serializable {

    /**
     * The type of the node (Hub or Spoke). This should always be a non negative value.
     */
    protected NodeType nodeType;

    /**
     * The node id. This should always be a non negative value.
     */
    protected int nodeId;

    public NodeId() {

    }

    public NodeId(NodeType nodeType, int nodeId) {
        checkNodeId(nodeId);
        this.nodeType = nodeType;
        this.nodeId = nodeId;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        checkNodeId(nodeId);
        this.nodeId = nodeId;
    }

    public void checkNodeId(int nodeId) {
        if (nodeId < 0)
            throw new RuntimeException("The id of a Node cannot be negative.");
    }

    public boolean isSpoke() {
        return nodeType.equals(NodeType.SPOKE);
    }

    public boolean isHub() {
        return !isSpoke();
    }

    public int getSize() {
        return 8;
    }

    @Override
    public String toString() {
        return (isSpoke() ? "SPOKE: " : "HUB: ") + nodeId;
    }
}
