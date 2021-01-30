package BipartiteTopologyAPI;

import BipartiteTopologyAPI.interfaces.Mergeable;
import BipartiteTopologyAPI.interfaces.Network;
import BipartiteTopologyAPI.interfaces.Node;
import BipartiteTopologyAPI.operations.RemoteCallIdentifier;
import BipartiteTopologyAPI.sites.NodeId;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

public class BufferingWrapper<D extends Serializable> extends GenericWrapper {

    private int maxBufferSize = 60000;
    private ArrayDeque<D> dataBuffer;
    private double meanBufferSize = 0;
    private long tuplesProcessed = 0;

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
            storePoint(tuple);
        } else {
            if (dataBuffer.isEmpty()) {
                super.receiveTuple(tuple);
            } else {
                storePoint(tuple);
                processFromDataBuffer();
            }
        }
        updateBufferStats();
    }

    private void storePoint(Serializable tuple) {
        dataBuffer.add((D) tuple);
        if (dataBuffer.size() == maxBufferSize + 1)
            dataBuffer.remove();
    }

    private void processFromDataBuffer() {
        while (!isBlocked() && !dataBuffer.isEmpty())
            super.receiveTuple(dataBuffer.pop());
    }

    private void updateBufferStats() {
        tuplesProcessed += 1;
        meanBufferSize = meanBufferSize + (1 / (1.0 * tuplesProcessed)) * (dataBuffer.size() - meanBufferSize);
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

    public double getMeanBufferSize() {
        return meanBufferSize;
    }

    public void setDataBuffer(ArrayDeque<D> dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

}
