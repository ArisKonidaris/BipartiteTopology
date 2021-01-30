package BipartiteTopologyAPI.operations;

import java.io.Serializable;

/**
 * A class that uniquely defines a remote procedure call in the Bipartite topology.
 */
public class RemoteCallIdentifier implements Serializable {

    CallType callType;
    String operation;
    long callNumber;

    public RemoteCallIdentifier() {
        this(null, null, -1);
    }

    public RemoteCallIdentifier(long callNumber) {
        this(CallType.RESPONSE, null, callNumber);
    }

    public RemoteCallIdentifier(CallType callType, String operation, long callNumber) {
        this.callType = callType;
        this.operation = operation;
        this.callNumber = callNumber;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Long getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(Long callNumber) {
        this.callNumber = callNumber;
    }

    public int getSize() {
        return 4 + 8 * operation.length() + 8;
    }

    @Override
    public String toString() {
        return "RemoteCallIdentifier(" + callType + ", " + operation + ", " + callNumber + ")";
    }
}
