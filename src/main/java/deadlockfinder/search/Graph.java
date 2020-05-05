package deadlockfinder.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Graph<S, L> {
    @Value
    public static class Edge<S, L> {
        S source;
        L label;
        S target;
    }

    final Map<S, String> names;
    final Map<S, Collection<EgressEdge<S, L>>> graphAsMap;

    @Getter
    final S initialNode;

    public int size() {
        return names.size();
    }

    public Iterable<S> getNodes() {
        return names.keySet();
    }

    public String getNodeName(S node) {
        return names.get(node);
    }

    public Iterable<String> getNodeNames() {
        return names.values();
    }

    public Iterable<Edge<S, L>> getEdges() {
        final Collection<Edge<S, L>> edges = new ArrayList<>();
        for (S s : graphAsMap.keySet()) {
            for (EgressEdge<S, L> egressEdge : graphAsMap.get(s)) {
                edges.add(new Edge<S, L>(s, egressEdge.getLabel(), egressEdge.getValue()));
            }
        }
        return edges;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Edge<S, L> edge : getEdges()) {
            sb.append(getNodeName(edge.getSource()));
            sb.append(" --");
            sb.append(edge.getLabel());
            sb.append("--> ");
            sb.append(getNodeName(edge.getTarget()));
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

}
