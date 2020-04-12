package deadlockfinder.model;

import static deadlockfinder.DeadLockFinder.trans;
import static deadlockfinder.Location.loc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import deadlockfinder.DeadLockFinder.Process;
import deadlockfinder.DeadLockFinder.ProcessBuilder;
import lombok.Value;
import lombok.With;

public class MInc2 extends ModelSupport<MInc2.SharedVars> {
    @Value
    @With
    public static class SharedVars {
        int x;
        int t1, t2;

        @Override
        public String toString() {
            return "x=" + x + " t1=" + t1 + " t2=" + t2;
        }
    }

    public static Process<SharedVars> p = new ProcessBuilder<SharedVars>()
        .with(loc("P0"), trans("read", "P1", r -> true, r -> r.withT1(r.getX())))
        .with(loc("P1"), trans("inc", "P2", r -> true, r -> r.withT1(r.getT1() + 1)))
        .with(loc("P2"), trans("write", "P3", r -> true, r -> r.withX(r.getT1())))
        .with(loc("P3"))
        .build();

    public static Process<SharedVars> q = new ProcessBuilder<SharedVars>()
        .with(loc("Q0"), trans("read", "Q1", r -> true, r -> r.withT2(r.getX())))
        .with(loc("Q1"), trans("inc", "Q2", r -> true, r -> r.withT2(r.getT2() + 1)))
        .with(loc("Q2"), trans("write", "Q3", r -> true, r -> r.withX(r.getT2())))
        .with(loc("Q3"))
        .build();

    @Override
    public SharedVars r0() {
        return new SharedVars(0, 0, 0);
    }

    @Override
    public List<Process<SharedVars>> processes() {
        return Arrays.asList(p, q);
    }

    public static void main(String[] args) throws IOException {
        new MInc2().run(args);
    }
}
