package deadlockfinder.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiPredicate;
import java.util.function.Function;

import lombok.Value;

@Value
class Todo<S> {
    private S state;
    private int id;
    private List<EgressEdge<S>> path;

    private static int nextId = 0;

    public static <S> Todo<S> of(S state, List<EgressEdge<S>> paths) {
        return new Todo<S>(state, nextId++, paths);
    }
}

public class BreadthFirstSearch<S> implements Search<S> {

    @Override
    public Graph<S> search(S initialState, Function<S, Collection<EgressEdge<S>>> next,
            BiPredicate<S, Collection<EgressEdge<S>>> pred, BiPredicate<S, Collection<EgressEdge<S>>> finish,
            Collection<List<EgressEdge<S>>> pathCollector) {
        final Map<S, String> names = new HashMap<>();
        names.put(initialState, "s0");
        final Map<S, Collection<EgressEdge<S>>> seen = new HashMap<>();
        seen.put(initialState, Collections.emptyList());
        final Queue<Todo<S>> agenda = new ArrayBlockingQueue<>(256);
        agenda.add(Todo.of(initialState, Arrays.asList(EgressEdge.of("---", initialState))));

        while (true) {
            final Todo<S> todo = agenda.poll();
            if (todo == null) {
                return new Graph<S>(names, seen);
            }
            final Collection<EgressEdge<S>> egressEdges = next.apply(todo.getState());
            if (pred.test(todo.getState(), egressEdges)) {
                pathCollector.add(todo.getPath());
            }
            seen.put(todo.getState(), egressEdges);
            for (EgressEdge<S> egressEdge : egressEdges) {
                if (!seen.containsKey(egressEdge.getValue())) {
                    seen.put(egressEdge.getValue(), Collections.emptyList());
                    names.put(egressEdge.getValue(), "s" + Integer.toString(names.size()));
                    final List<EgressEdge<S>> newPath = new ArrayList<>();
                    newPath.addAll(todo.getPath());
                    newPath.add(egressEdge);
                    agenda.offer(Todo.of(egressEdge.getValue(), Collections.unmodifiableList(newPath)));
                }
            }
        }
    }
}
