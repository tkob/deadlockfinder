package deadlockfinder.sharedvar.model;

import static deadlockfinder.sharedvar.DeadLockFinder.trans;
import static deadlockfinder.sharedvar.Location.loc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import deadlockfinder.sharedvar.DeadLockFinder.Process;
import deadlockfinder.sharedvar.DeadLockFinder.ProcessBuilder;
import lombok.Value;
import lombok.With;

public class MMutex2 extends ModelSupport<MMutex2.SharedVars> {
    @Value
    @With
    public static class SharedVars {
        int m0, m1;

        @Override
        public String toString() {
            return "m0=" + m0 + " m1=" + m1;
        }
    }

    public static Process<SharedVars> p = new ProcessBuilder<SharedVars>()
        .with(loc("P0"), trans("lock 0", "P1", r -> r.getM0() == 0, r -> r.withM0(1)))
        .with(loc("P1"), trans("lock 1", "P2", r -> r.getM1() == 0, r -> r.withM1(1)))
        .with(loc("P2"), trans("unlock 1", "P3", r -> true, r -> r.withM1(0)))
        .with(loc("P3"), trans("unlock 0", "P0", r -> true, r -> r.withM0(0)))

        .build();

    public static Process<SharedVars> q = new ProcessBuilder<SharedVars>()
        .with(loc("Q0"), trans("lock 1", "Q1", r -> r.getM1() == 0, r -> r.withM1(1)))
        .with(loc("Q1"), trans("lock 0", "Q2", r -> r.getM0() == 0, r -> r.withM0(1)))
        .with(loc("Q2"), trans("unlock 0", "Q3", r -> true, r -> r.withM0(0)))
        .with(loc("Q3"), trans("unlock 1", "Q0", r -> true, r -> r.withM1(0)))
        .build();

    @Override
    public SharedVars r0() {
        return new SharedVars(0, 0);
    }

    @Override
    public List<Process<SharedVars>> processes() {
        return Arrays.asList(p, q);
    }

    public static void main(String[] args) throws IOException {
        new MMutex2().run(args);
    }
}
