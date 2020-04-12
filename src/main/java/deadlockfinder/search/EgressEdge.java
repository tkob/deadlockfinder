package deadlockfinder.search;

import lombok.Value;

@Value(staticConstructor = "of")
public class EgressEdge<T> {
    private String label;
    private T value;
}
