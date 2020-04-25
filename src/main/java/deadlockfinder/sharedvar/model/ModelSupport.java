package deadlockfinder.sharedvar.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import deadlockfinder.search.Path;
import deadlockfinder.sharedvar.DeadLockFinder;
import deadlockfinder.sharedvar.DeadLockFinder.Lts;
import deadlockfinder.sharedvar.DeadLockFinder.Process;
import deadlockfinder.sharedvar.DeadLockFinder.State;

public abstract class ModelSupport<R> {

    public abstract R r0();

    public abstract List<Process<R>> processes();

    private final DeadLockFinder deadLockFinder = new DeadLockFinder();

    public void run(String[] args) throws IOException {
        final Lts<R> lts = deadLockFinder.concurrentComposition(r0(), processes());

        for (Path<State<R>, String> deadLockPath : lts.getDeadLockPaths()) {
            System.out.println("------------");
            deadLockPath.print(new PrintWriter(System.out));
        }
        System.out.println("------------");

        if (args.length > 0) {
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(args[0]))) {
                lts.printDot(writer);
            }
        } else {
            lts.printDot(new PrintWriter(System.out));
        }
    }

}
