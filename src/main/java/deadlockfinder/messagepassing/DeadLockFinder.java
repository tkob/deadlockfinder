package deadlockfinder.messagepassing;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.collections4.SetUtils;

import deadlockfinder.search.BreadthFirstSearch;
import deadlockfinder.search.EgressEdge;
import deadlockfinder.search.Graph;
import deadlockfinder.search.Path;
import deadlockfinder.search.Search;
import lombok.Value;

public class DeadLockFinder {

    /**
     * Representation of process definition
     * @param <S> State
     */
    public abstract static class Process<S, E> {
        abstract public Collection<EgressEdge<S, E>> next(S state);

        abstract public S initialState();
    }

    public <S, E> Graph<S, E> unfold(S s0, Process<S, E> process) {
        final Search<S, E> search = new BreadthFirstSearch<S, E>();
        final Collection<Path<S, E>> deadLockPaths = new ArrayList<>();
        return search.search(
            s0,
            process::next,
            (s, egressEdges) -> egressEdges.isEmpty(),
            (s, egressEdges) -> false,
            deadLockPaths);
    }

    @Value(staticConstructor = "of")
    public static class ComposedState<S> {
        S left;
        S right;
    }

    @Value
    public static class Lts<S, E> {
        final Graph<S, E> states;
        final Collection<Path<S, E>> deadLockPaths;

        public void printDot(PrintWriter writer) {
            writer.println("digraph {");
            for (S state : states.getNodes()) {
                writer.print("    ");
                writer.print(states.getNodeName(state));
                writer.print(" [label=\"");
                writer.print(states.getNodeName(state));
                writer.println("\\\"];");
            }
            for (Graph.Edge<S, E> edge : states.getEdges()) {
                writer.print("    ");
                writer.print(states.getNodeName(edge.getSource()));
                writer.print(" -> ");
                writer.print(states.getNodeName(edge.getTarget()));
                writer.print(" [label=\"");
                writer.print(edge.getLabel());
                writer.println("\"];");
            }
            writer.println();
            writer.println("}");
            writer.flush();
        }
    }

    public <S, E> Lts<ComposedState<S>, E> concurrencComposition(Predicate<E> sync, Graph<S, E> pGraph,
            Graph<S, E> qGraph) {
        final ComposedState<S> s0 = ComposedState.of(pGraph.getInitialNode(), qGraph.getInitialNode());
        final Function<ComposedState<S>, Collection<EgressEdge<ComposedState<S>, E>>> next = (s -> {
            final S pCurrentState = s.getLeft();
            final S qCurrentState = s.getRight();
            final Map<E, Collection<S>> synchtP = new HashMap<>();
            final Map<E, Collection<S>> synchtQ = new HashMap<>();
            final Set<EgressEdge<ComposedState<S>, E>> trans = new HashSet<>();
            for (EgressEdge<S, E> egressEdges : pGraph.getEgressEdges(pCurrentState)) {
                final E event = egressEdges.getLabel();
                final S target = egressEdges.getValue();
                if (sync.test(event)) {
                    if (!synchtP.containsKey(event)) {
                        synchtP.put(event, new ArrayList<S>(Arrays.asList(target)));
                    }
                    synchtP.get(event).add(target);
                } else {
                    // unsynchronized event: interleave transition
                    trans.add(EgressEdge.of(event, ComposedState.of(target, qCurrentState)));
                }
            }
            for (EgressEdge<S, E> egressEdges : qGraph.getEgressEdges(qCurrentState)) {
                final E event = egressEdges.getLabel();
                final S target = egressEdges.getValue();
                if (sync.test(event)) {
                    if (!synchtQ.containsKey(event)) {
                        synchtQ.put(event, new ArrayList<S>(Arrays.asList(target)));
                    }
                    synchtQ.get(event).add(target);
                } else {
                    // unsynchronized event: interleave transition
                    trans.add(EgressEdge.of(event, ComposedState.of(pCurrentState, target)));
                }
            }
            // Up to this point, trans contains all unsynchronized transitions
            // and synchtP and synchtQ contain possibly synchronized events and their targets.

            // Transitions for an event exist only if both component states have transitions for the event
            for (E event : SetUtils.intersection(synchtP.keySet(), synchtQ.keySet())) {
                // Add all combinations
                for (S pState : synchtP.get(event)) {
                    for (S qState : synchtQ.get(event)) {
                        trans.add(EgressEdge.of(event, ComposedState.of(pState, qState)));
                    }
                }
            }
            return trans;
        });
        final Search<ComposedState<S>, E> search = new BreadthFirstSearch<ComposedState<S>, E>();
        final List<Path<ComposedState<S>, E>> deadLockPaths = new ArrayList<>();
        final Graph<ComposedState<S>, E> graph = search.search(
            s0,
            next,
            (s, egressEdges) -> egressEdges.isEmpty(),
            (s, egressEdges) -> false,
            deadLockPaths);
        return new Lts<ComposedState<S>, E>(graph, deadLockPaths);
    }
}
