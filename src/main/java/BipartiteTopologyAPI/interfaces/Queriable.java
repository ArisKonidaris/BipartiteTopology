package BipartiteTopologyAPI.interfaces;

import java.io.Serializable;

public interface Queriable extends Serializable {

    void query(long queryId, int queried, Serializable[] buffer);

}