package BipartiteTopologyAPI.futures;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * A class representing promised responses.
 *
 * @param <T> The type of the Serializable response values.
 */
public class PromisedResponses<T extends Serializable> implements Response<T> {

    /** The promises made to disjoint node in the bipartite network. */
    private List<PromiseResponse<T>> promisedResponses;

    /** The answers to the promises made to the disjoint node in the bipartite network. */
    private List<T> answers;

    public PromisedResponses() {

    }

    @Override
    public void to(Consumer<T> consumer) {
        throw new UnsupportedOperationException("to() called on PromisedResponses");
    }

    @Override
    public void toSync(Consumer<T> consumer) {
        throw new UnsupportedOperationException("toSync() called on PromisedResponses");
    }

    @Override
    public T getValue() {
        throw new UnsupportedOperationException("getValue() called on PromisedResponses");
    }

    public void sendAnswers() {
        Iterator<T> answer = answers.iterator();
        Iterator<PromiseResponse<T>> promise = promisedResponses.iterator();
        while (answer.hasNext() && promise.hasNext())
            promise.next().sendAnswer(answer.next());
    }

}
