package deadlockfinder.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Graph<S> {
    @Value
    public static class Edge<S> {
        S source;
        String label;
        S target;
    }

    final Map<S, String> names;
    final Map<S, Collection<EgressEdge<S>>> graphAsMap;

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

    public Iterable<Edge<S>> getEdges() {
        final Collection<Edge<S>> edges = new ArrayList<>();
        for (S s : graphAsMap.keySet()) {
            for (EgressEdge<S> egressEdge : graphAsMap.get(s)) {
                edges.add(new Edge<S>(s, egressEdge.getLabel(), egressEdge.getValue()));
            }
        }
        return edges;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Edge<S> edge : getEdges()) {
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
