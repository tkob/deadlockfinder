package deadlockfinder.search;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class Path<S, L> {
    @Getter
    private final S initial;
    private final List<EgressEdge<S, L>> rest;

    public static <S, L> Path<S, L> of(S initial) {
        return new Path<S, L>(initial, Collections.emptyList());
    }

    public Path<S, L> add(L label, S target) {
        final List<EgressEdge<S, L>> newRest = new ArrayList<>();
        for (EgressEdge<S, L> egressEdge : rest) {
            newRest.add(egressEdge);
        }
        newRest.add(EgressEdge.of(label, target));
        return new Path<S, L>(initial, Collections.unmodifiableList(newRest));
    }

    public void print(PrintWriter writer) {
        int i = 0;
        writer.print(StringUtils.leftPad(Integer.toString(i++), 3));
        writer.print(' ');
        writer.print(StringUtils.rightPad("---", 8));
        writer.print(' ');
        writer.println(initial);
        for (EgressEdge<S, L> egressEdge : rest) {
            writer.print(StringUtils.leftPad(Integer.toString(i++), 3));
            writer.print(' ');
            writer.print(StringUtils.rightPad(egressEdge.getLabel().toString(), 8));
            writer.print(' ');
            writer.println(egressEdge.getValue());
        }
        writer.flush();
    }
}
