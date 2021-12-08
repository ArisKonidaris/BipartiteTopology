package BipartiteTopologyAPI;

import BipartiteTopologyAPI.annotations.RemoteOp;
import BipartiteTopologyAPI.futures.FuturePool;
import BipartiteTopologyAPI.futures.FutureResponse;
import BipartiteTopologyAPI.futures.Response;
import BipartiteTopologyAPI.interfaces.Network;
import BipartiteTopologyAPI.operations.CallType;
import BipartiteTopologyAPI.operations.RemoteCallIdentifier;
import BipartiteTopologyAPI.sites.NodeId;
import BipartiteTopologyAPI.sites.NodeType;
import com.fasterxml.uuid.Generators;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * A Generic Proxy.
 */
public class GenericProxy implements InvocationHandler, Serializable {

    /**
     * The wrapped node object that possesses this proxy.
     */
    private final GenericWrapper nodeWrapper;

    /**
     * The network used by this proxy to send messages to remote nodes.
     */
    private final Network network;

    /**
     * The targeted remote node.
     */
    private final NodeId target;

    /**
     * A counter for identifying the responses of the remote nodes.
     */
    private long futureCounter;

    /**
     * The ids of the proxied methods.
     */
    private Map<Method, String> methodIds;

    /** The names of the proxied methods */
    private ArrayList<String> methodNames;

    @Override
    public Object invoke(Object o, Method method, Object[] args) {
        Response<Serializable> response = null;
        RemoteCallIdentifier rpc = new RemoteCallIdentifier();

        rpc.setOperation(methodIds.get(method));
        boolean hasResponse = method.getReturnType().equals(Response.class);

        try {
            if (target == null) {
                network.send(nodeWrapper.nodeId, null, rpc, args);
            } else if (target.getNodeId() == Integer.MAX_VALUE) {
                int targets = (isSpoke()) ? numberOfHubs() : numberOfSpokes();
                if (hasResponse) {
                    Map<Integer, FutureResponse<Serializable>> newFutures = new HashMap<>();
                    for (int i = 0; i < targets; i++) {
                        FutureResponse<Serializable> newFuture = new FutureResponse<>();
                        newFutures.put(i, newFuture);
                        nodeWrapper.getNewFutures().add(newFuture);
                    }
                    assert !nodeWrapper.getFutures().containsKey(futureCounter);
                    nodeWrapper.getFutures().put(futureCounter, newFutures);
                    response = new FuturePool<>(newFutures.values());
                    rpc.setCallType(CallType.TWO_WAY);
                    rpc.setCallNumber(futureCounter);
                    if (futureCounter == Long.MAX_VALUE)
                        futureCounter = 0L;
                    else
                        futureCounter++;
                } else {
                    rpc.setCallType(CallType.ONE_WAY);
                }
                Map<NodeId, RemoteCallIdentifier> rpcs = new HashMap<>();
                for (int i = 0; i < targets; i++) {
                    rpcs.put(new NodeId((isSpoke()) ? NodeType.HUB : NodeType.SPOKE, i), rpc);
                }
                network.broadcast(nodeWrapper.getNodeId(), rpcs, args);
            } else {
                if (hasResponse) {
                    response = new FutureResponse<>();
                    if (!nodeWrapper.getFutures().containsKey(futureCounter)) {
                        Map<Integer, FutureResponse<Serializable>> newFuture = new HashMap<>();
                        newFuture.put(target.getNodeId(), (FutureResponse<Serializable>) response);
                        nodeWrapper.getFutures().put(futureCounter, newFuture);
                    } else {
                        assert !nodeWrapper.getFutures().get(futureCounter).containsKey(target.getNodeId());
                        nodeWrapper.getFutures().get(futureCounter)
                                .put(target.getNodeId(), (FutureResponse<Serializable>) response);
                    }
                    nodeWrapper.getNewFutures().add((FutureResponse<Serializable>) response);
                    rpc.setCallType(CallType.TWO_WAY);
                    rpc.setCallNumber(futureCounter);
                    if (futureCounter == Long.MAX_VALUE)
                        futureCounter = 0L;
                    else
                        futureCounter++;
                } else {
                    rpc.setCallType(CallType.ONE_WAY);
                }
                network.send(nodeWrapper.getNodeId(), target, rpc, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public GenericProxy(Class rmtIf, GenericWrapper node_wrapper, Network network, NodeId target) {
        this.nodeWrapper = node_wrapper;
        this.network = network;
        this.target = target;
        futureCounter = 0L;
        methodIds = new HashMap<>();
        methodNames = new ArrayList<>();

        ArrayList<Method> methods = new ArrayList<>(Arrays.asList(rmtIf.getDeclaredMethods()));
        for (Method method : methods) {
            NodeClass.check(method.getDeclaredAnnotation(RemoteOp.class) != null,
                    "Method %s is not annotated with @RemoteOp", method);
            for (Parameter param : method.getParameters()) {
                Class pcls = param.getType();
                NodeClass.check(NodeClass.isSerializable(pcls),
                        "Parameter type %s is not Serializable request method %s of remote proxy %s",
                        pcls, method, rmtIf);
            }
            NodeClass.check(method.getReturnType() == void.class || method.getReturnType() == Response.class,
                    "Return type is not void request method %s of remote proxy %s",
                    method, rmtIf);
            String methodName = method.getName() + Arrays.toString(method.getParameterTypes());
            String methodId = Generators.nameBasedGenerator().generate(methodName).toString();
            methodIds.put(method, methodId);
            methodNames.add(methodName);
        }
    }


    private boolean isSpoke() {
        return nodeWrapper.getNodeId().isSpoke();
    }

    private int numberOfSpokes() {
        return network.describe().getNumberOfSpokes();
    }

    private int numberOfHubs() {
        return network.describe().getNumberOfHubs();
    }

    static public <RmtIf> RmtIf forNode(Class<RmtIf> cls, GenericWrapper node_wrapper, Network network, NodeId target) {
        GenericProxy proxy = new GenericProxy(cls, node_wrapper, network, target);
        return (RmtIf) Proxy.newProxyInstance(GenericProxy.class.getClassLoader(), new Class<?>[]{cls}, proxy);
    }

    public GenericWrapper getNodeWrapper() {
        return nodeWrapper;
    }

    public Network getNetwork() {
        return network;
    }

    public NodeId getTarget() {
        return target;
    }

    public long getFutureCounter() {
        return futureCounter;
    }

    public Map<Method, String> getMethodIds() {
        return Collections.unmodifiableMap(methodIds);
    }

}
