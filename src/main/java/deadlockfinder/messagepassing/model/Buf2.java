package deadlockfinder.messagepassing.model;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import deadlockfinder.messagepassing.DeadLockFinder;
import deadlockfinder.messagepassing.DeadLockFinder.ComposedState;
import deadlockfinder.messagepassing.DeadLockFinder.Lts;
import deadlockfinder.messagepassing.DeadLockFinder.Process;
import deadlockfinder.search.EgressEdge;
import deadlockfinder.search.Graph;
import lombok.Value;

public class Buf2 {
    public static interface Event {
        boolean accept(EventVisitor visitor);
    }

    public static interface EventVisitor {
        boolean visitIn(In in);

        boolean visitOut(Out out);

        boolean visitMid(Mid mid);
    }

    @Value(staticConstructor = "of")
    public static class In implements Event {
        int x;

        @Override
        public boolean accept(EventVisitor visitor) {
            return visitor.visitIn(this);
        }
    }

    @Value(staticConstructor = "of")
    public static class Out implements Event {
        int x;

        @Override
        public boolean accept(EventVisitor visitor) {
            return visitor.visitOut(this);
        }
    }

    @Value(staticConstructor = "of")
    public static class Mid implements Event {
        int x;

        @Override
        public boolean accept(EventVisitor visitor) {
            return visitor.visitMid(this);
        }
    }

    public static interface State {
        Collection<EgressEdge<State, Event>> accept(StateVisitor visitor);
    }

    public static interface StateVisitor {
        Collection<EgressEdge<State, Event>> visitS0(S0 s0);

        Collection<EgressEdge<State, Event>> visitS1(S1 s1);
    }

    @Value(staticConstructor = "of")
    public static class S0 implements State {

        @Override
        public Collection<EgressEdge<State, Event>> accept(StateVisitor visitor) {
            return visitor.visitS0(this);
        }
    }

    @Value(staticConstructor = "of")
    public static class S1 implements State {
        int x;

        @Override
        public Collection<EgressEdge<State, Event>> accept(StateVisitor visitor) {
            return visitor.visitS1(this);
        }
    }

    private static int[] range = { 0, 1, 2 };

    public static Process<State, Event> p = new Process<State, Event>() {

        @Override
        public Collection<EgressEdge<State, Event>> next(State state) {
            return state.accept(new StateVisitor() {

                @Override
                public Collection<EgressEdge<State, Event>> visitS0(S0 s0) {
                    return Arrays.stream(range)
                        .<EgressEdge<State, Event>> mapToObj(x -> EgressEdge.of(In.of(x), S1.of(x)))
                        .collect(Collectors.toList());
                }

                @Override
                public Collection<EgressEdge<State, Event>> visitS1(S1 s1) {
                    return Arrays.asList(EgressEdge.of(Mid.of(s1.getX()), S0.of()));
                }
            });
        }

        public State initialState() {
            return S0.of();
        }
    };

    static Process<State, Event> q = new Process<State, Event>() {

        @Override
        public Collection<EgressEdge<State, Event>> next(State state) {
            return state.accept(new StateVisitor() {

                @Override
                public Collection<EgressEdge<State, Event>> visitS0(S0 s0) {
                    return Arrays.stream(range)
                        .<EgressEdge<State, Event>> mapToObj(x -> EgressEdge.of(Mid.of(x), S1.of(x)))
                        .collect(Collectors.toList());
                }

                @Override
                public Collection<EgressEdge<State, Event>> visitS1(S1 s1) {
                    return Arrays.asList(EgressEdge.of(Out.of(s1.getX()), S0.of()));
                }
            });
        }

        public State initialState() {
            return S0.of();
        }
    };

    static Predicate<Event> sync = event -> event.accept(new EventVisitor() {

        @Override
        public boolean visitIn(In in) {
            return false;
        }

        @Override
        public boolean visitOut(Out out) {
            return false;
        }

        @Override
        public boolean visitMid(Mid mid) {
            return true;
        }
    });

    private final DeadLockFinder deadLockFinder = new DeadLockFinder();

    public void run(String[] args) {
        Graph<State, Event> ltsP = deadLockFinder.unfold(S0.of(), p);
        Graph<State, Event> ltsQ = deadLockFinder.unfold(S0.of(), q);
        final Lts<ComposedState<State>, Event> lts = deadLockFinder.concurrencComposition(sync, ltsP, ltsQ);
        System.err.println(lts.getDeadLockPaths());
        lts.printDot(new PrintWriter(System.out));
    }

    public static void main(String[] args) {
        new Buf2().run(args);
    }

}
