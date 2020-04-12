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
public class Path<S> {
    @Getter
    private final S initial;
    private final List<EgressEdge<S>> rest;

    public static <S> Path<S> of(S initial) {
        return new Path<S>(initial, Collections.emptyList());
    }

    public Path<S> add(String label, S target) {
        final List<EgressEdge<S>> newRest = new ArrayList<>();
        for (EgressEdge<S> egressEdge : rest) {
            newRest.add(egressEdge);
        }
        newRest.add(EgressEdge.of(label, target));
        return new Path<S>(initial, Collections.unmodifiableList(newRest));
    }

    public void print(PrintWriter writer) {
        int i = 0;
        writer.print(StringUtils.leftPad(Integer.toString(i++), 3));
        writer.print(' ');
        writer.print(StringUtils.rightPad("---", 8));
        writer.print(' ');
        writer.println(initial);
        for (EgressEdge<S> egressEdge : rest) {
            writer.print(StringUtils.leftPad(Integer.toString(i++), 3));
            writer.print(' ');
            writer.print(StringUtils.rightPad(egressEdge.getLabel(), 8));
            writer.print(' ');
            writer.println(egressEdge.getValue());
        }
        writer.flush();
    }
}
