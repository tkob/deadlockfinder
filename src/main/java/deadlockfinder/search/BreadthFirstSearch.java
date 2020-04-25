package deadlockfinder.search;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiPredicate;
import java.util.function.Function;

import lombok.Value;

@Value
class Todo<S, L> {
    private S state;
    private int id;
    private Path<S, L> path;

    private static int nextId = 0;

    public static <S, L> Todo<S, L> of(S state, Path<S, L> path) {
        return new Todo<S, L>(state, nextId++, path);
    }
}

public class BreadthFirstSearch<S, L> implements Search<S, L> {

    @Override
    public Graph<S, L> search(S initialState, Function<S, Collection<EgressEdge<S, L>>> next,
            BiPredicate<S, Collection<EgressEdge<S, L>>> pred,
            BiPredicate<S, Collection<EgressEdge<S, L>>> finish, Collection<Path<S, L>> pathCollector) {
        final Map<S, String> names = new HashMap<>();
        names.put(initialState, "s0");
        final Map<S, Collection<EgressEdge<S, L>>> seen = new HashMap<>();
        seen.put(initialState, Collections.emptyList());
        final Queue<Todo<S, L>> agenda = new ArrayBlockingQueue<>(256);
        agenda.add(Todo.of(initialState, Path.of(initialState)));

        while (true) {
            final Todo<S, L> todo = agenda.poll();
            if (todo == null) {
                return new Graph<S, L>(names, seen);
            }
            final Collection<EgressEdge<S, L>> egressEdges = next.apply(todo.getState());
            if (pred.test(todo.getState(), egressEdges)) {
                pathCollector.add(todo.getPath());
            }
            if (finish.test(todo.getState(), egressEdges)) {
                return new Graph<S, L>(names, seen);
            }
            seen.put(todo.getState(), egressEdges);
            for (EgressEdge<S, L> egressEdge : egressEdges) {
                if (!seen.containsKey(egressEdge.getValue())) {
                    seen.put(egressEdge.getValue(), Collections.emptyList());
                    names.put(egressEdge.getValue(), "s" + Integer.toString(names.size()));
                    final Path<S, L> newPath =
                        todo.getPath().add(egressEdge.getLabel(), egressEdge.getValue());
                    agenda.offer(Todo.of(egressEdge.getValue(), newPath));
                }
            }
        }
    }
}
