package deadlockfinder.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import deadlockfinder.DeadLockFinder;
import deadlockfinder.DeadLockFinder.Lts;
import deadlockfinder.DeadLockFinder.Process;
import deadlockfinder.DeadLockFinder.State;
import deadlockfinder.search.Path;

public abstract class ModelSupport<R> {

    public abstract R r0();

    public abstract List<Process<R>> processes();

    private final DeadLockFinder deadLockFinder = new DeadLockFinder();

    public void run(String[] args) throws IOException {
        final Lts<R> lts = deadLockFinder.concurrentComposition(r0(), processes());

        for (Path<State<R>> deadLockPath : lts.getDeadLockPaths()) {
            deadLockPath.print(new PrintWriter(System.out));
        }

        if (args.length > 0) {
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(args[0]))) {
                lts.printDot(writer);
            }
        } else {
            lts.printDot(new PrintWriter(System.out));
        }
    }

}
