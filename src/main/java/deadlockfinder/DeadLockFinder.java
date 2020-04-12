package deadlockfinder;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import deadlockfinder.search.BreadthFirstSearch;
import deadlockfinder.search.EgressEdge;
import deadlockfinder.search.Graph;
import deadlockfinder.search.Path;
import deadlockfinder.search.Search;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.With;

public class DeadLockFinder {

    @ToString
    @RequiredArgsConstructor(staticName = "of")
    @Getter
    public static class Label {
        private final String label;
    }

    @Value(staticConstructor = "of")
    public static class Transition<R> {
        Label label;
        Location target;
        Predicate<R> guard;
        final Function<R, R> action;
    }

    @Value(staticConstructor = "of")
    public static class Process<R> {
        Location initialLocation;
        Map<Location, Set<Transition<R>>> locationToTransitions;

        public ProcessCursor<R> initial() {
            return new ProcessCursor<R>(initialLocation, this);
        }

        public Set<Transition<R>> transitionsFromLocation(Location location) {
            return locationToTransitions.get(location);
        }

        public void printDot(PrintWriter writer) {
            writer.println("digraph {");
            for (Location location : locationToTransitions.keySet()) {
                writer.print("    ");
                writer.print(location);
                writer.println(";");
            }
            writer.println();
            for (Location location : locationToTransitions.keySet()) {
                for (Transition<R> transition : locationToTransitions.get(location)) {
                    writer.print("    ");
                    writer.print(location);
                    writer.print(" -> ");
                    writer.print(transition.getTarget());
                    writer.print(" [label=\"");
                    writer.print(transition.getLabel());
                    writer.println("\"];");
                }
            }
            writer.println("}");
            writer.flush();
        }
    }

    @Value
    public static class ProcessCursor<R> {
        @With
        Location location;
        Process<R> process;

        @Override
        public String toString() {
            return location.toString();
        }
    }

    public static class ProcessBuilder<R> {
        private final List<Location> locations = new ArrayList<>();
        private final Map<Location, Set<Transition<R>>> locationToTransitions = new HashMap<>();

        @SafeVarargs
        public final ProcessBuilder<R> with(Location location, Transition<R>... transitions) {
            locations.add(location);
            locationToTransitions.put(
                location,
                Collections.unmodifiableSet(Arrays.asList(transitions).stream().collect(Collectors.toSet())));
            return this;
        }

        public Process<R> build() {
            final Location initialLocation = locations.get(0);
            return new Process<R>(initialLocation, Collections.unmodifiableMap(locationToTransitions));
        }
    }

    public static <R> Transition<R> trans(String label, String target, Predicate<R> guard,
            Function<R, R> action) {
        return Transition.of(Label.of(label), Location.of(target), guard, action);
    }

    /**
     * Composed state of multiple processes.
     * @param <R> Type of a record of shared variables
     */
    @Value(staticConstructor = "of")
    public static class State<R> {
        R variables;
        List<ProcessCursor<R>> processCursors;

        public boolean isDeadLock() {
            return true;
        }
    }

    @Value
    public static class Lts<R> {
        final Graph<State<R>> states;
        final Collection<Path<State<R>>> deadLockPaths;

        public void printDot(PrintWriter writer) {
            writer.println("digraph {");
            for (State<R> state : states.getNodes()) {
                writer.print("    ");
                writer.print(states.getNodeName(state));
                writer.print(" [label=\"");
                writer.print(states.getNodeName(state));
                writer.print("\\n");
                state.getProcessCursors().stream().map(ProcessCursor::getLocation).forEach(location -> {
                    writer.print(location);
                    writer.print(" ");
                });
                writer.print("\\n");
                writer.print(state.getVariables());
                writer.print("\",");
                if (true) {
                    // TODO
                }
                writer.println("];");
            }
            for (Graph.Edge<State<R>> edge : states.getEdges()) {
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

    @SafeVarargs
    public final <R> Lts<R> concurrentComposition(R r0, Process<R>... processes) {
        return concurrentComposition(r0, Arrays.asList(processes));
    }

    public final <R> Lts<R> concurrentComposition(R r0, List<Process<R>> processes) {
        final State<R> s0 =
            State.of(r0, processes.stream().map(Process::initial).collect(Collectors.toList()));
        final Function<State<R>, Collection<EgressEdge<State<R>>>> next = s -> {
            final Map<Label, State<R>> labelToState = new HashMap<>();
            for (ProcessCursor<R> processCursor : s.getProcessCursors()) {
                final Location location = processCursor.getLocation();
                final Process<R> process = processCursor.getProcess();
                final Set<Transition<R>> transitions = process.transitionsFromLocation(location);
                for (Transition<R> transition : transitions) {
                    if (transition.getGuard().test(s.getVariables())) {
                        final R nextVariables = transition.getAction().apply(s.getVariables());
                        final List<ProcessCursor<R>> nextProcessCursors =
                            s.getProcessCursors().stream().map(pc -> {
                                if (processCursor.getLocation().equals(pc.getLocation())) {
                                    return pc.withLocation(transition.getTarget());
                                } else {
                                    return pc;
                                }
                            }).collect(Collectors.toList());
                        final State<R> nextState = State.of(nextVariables, nextProcessCursors);
                        labelToState.put(transition.getLabel(), nextState);
                    }
                }
            }
            return labelToState.keySet()
                .stream()
                .map(label -> EgressEdge.of(label.getLabel(), labelToState.get(label)))
                .collect(Collectors.toSet());
        };
        final Search<State<R>> search = new BreadthFirstSearch<State<R>>();

        final Collection<Path<State<R>>> deadLockPaths = new ArrayList<>();
        final Graph<State<R>> graph = search.search(
            s0,
            next,
            (s, egressEdges) -> egressEdges.isEmpty(),
            (s, egressEdges) -> false,
            deadLockPaths);
        return new Lts<R>(graph, deadLockPaths);
    }
}
