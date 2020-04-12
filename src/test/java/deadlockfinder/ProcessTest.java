package deadlockfinder;

import static deadlockfinder.DeadLockFinder.trans;
import static deadlockfinder.Location.loc;

import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import deadlockfinder.DeadLockFinder.Process;
import deadlockfinder.DeadLockFinder.ProcessBuilder;
import deadlockfinder.model.MInc2;

class ProcessTest {

    @Test
    void testPrintDot() {
        final Process<MInc2.SharedVars> sut = new ProcessBuilder<MInc2.SharedVars>()
            .with(loc("P0"), trans("read", "P1", r -> true, r -> r.withT1(r.getX())))
            .with(loc("P1"), trans("inc", "P2", r -> true, r -> r.withT1(r.getT1() + 1)))
            .with(loc("P2"), trans("write", "P3", r -> true, r -> r.withX(r.getT1())))
            .with(loc("P3"))
            .build();
        sut.printDot(new PrintWriter(System.err));
        System.err.println(sut);
    }

}
