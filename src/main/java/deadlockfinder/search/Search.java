package deadlockfinder.search;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public interface Search<S> {

    /**
     * Find states which are reachable from initial state and satisfy specified predicate.
     * @param initialState Initial state of search
     * @param next A function from state to collection of outgoing edges
     * @param pred A predicate which specifies what to find
     * @param pathCollector Collection of paths to states which satisfy the predicate
     */
    Graph<S> search(S initialState, Function<S, Collection<EgressEdge<S>>> next,
            BiPredicate<S, Collection<EgressEdge<S>>> pred, BiPredicate<S, Collection<EgressEdge<S>>> finish,
            Collection<List<EgressEdge<S>>> pathCollector);

}
