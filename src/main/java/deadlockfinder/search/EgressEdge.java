package deadlockfinder.search;

import lombok.Value;

@Value(staticConstructor = "of")
public class EgressEdge<V, L> {
    private L label;
    private V value;
}
