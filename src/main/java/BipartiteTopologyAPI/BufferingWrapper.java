package BipartiteTopologyAPI;

import BipartiteTopologyAPI.network.Mergeable;
import BipartiteTopologyAPI.network.Network;
import BipartiteTopologyAPI.network.Node;
import BipartiteTopologyAPI.operations.RemoteCallIdentifier;
import BipartiteTopologyAPI.sites.NodeId;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

public class BufferingWrapper<D extends Serializable> extends GenericWrapper {

    private ArrayDeque<D> dataBuffer;

    public BufferingWrapper(NodeId nodeId, NodeInstance node, Network network) {
        super(nodeId, node, network);
        this.dataBuffer = new ArrayDeque<>();
    }

    public void toggle() {
        processFromDataBuffer();
    }

    @Override
    public void receiveMsg(NodeId source, RemoteCallIdentifier rpc, Serializable tuple) {
        super.receiveMsg(source, rpc, tuple);
        processFromDataBuffer();
    }

    @Override
    public void receiveTuple(Serializable tuple) {
        if (isBlocked()) {
            dataBuffer.add((D) tuple);
        } else {
            if (dataBuffer.isEmpty()) {
                super.receiveTuple(tuple);
            } else {
                dataBuffer.add((D) tuple);
                processFromDataBuffer();
            }
        }
    }

    private void processFromDataBuffer() {
        while (!isBlocked() && !dataBuffer.isEmpty())
            super.receiveTuple(dataBuffer.pop());
    }

    @Override
    public void merge(Mergeable[] nodes) {

        // Merge the nodes.
        super.merge(nodes);

        // Get all the data buffers.
        ArrayList<ArrayDeque<D>> listOfBuffers = new ArrayList<>();
        listOfBuffers.add(dataBuffer);
        for (Node node : (Node[]) nodes) {
            assert (node instanceof BufferingWrapper);
            listOfBuffers.add(((BufferingWrapper) node).getDataBuffer());
        }

        // Merge all the data buffers by interpolating their data.
        ArrayDeque<D> mergedBuffer = new ArrayDeque<>();
        while(!listOfBuffers.isEmpty()) {
            Iterator<ArrayDeque<D>> i = listOfBuffers.iterator();
            while (i.hasNext()) {
                ArrayDeque<D> buffer = i.next();
                if (!buffer.isEmpty())
                    mergedBuffer.add(buffer.pop());
                 else
                    buffer.remove();

            }
        }
        dataBuffer = mergedBuffer;
    }

    public ArrayDeque<D> getDataBuffer() {
        return dataBuffer;
    }

    public void setDataBuffer(ArrayDeque<D> dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

}
