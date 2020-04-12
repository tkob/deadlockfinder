package deadlockfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import deadlockfinder.search.BreadthFirstSearch;
import deadlockfinder.search.EgressEdge;
import deadlockfinder.search.Graph;
import deadlockfinder.search.Search;
import lombok.Value;

class BreadthFirstSearchTest {
    private interface Node<E> {
        <R> R accept(NodeVisitor<E, R> visitor);
    }

    @Value(staticConstructor = "of")
    private static class Leaf<E> implements Node<E> {
        private E value;

        @Override
        public <R> R accept(NodeVisitor<E, R> visitor) {
            return visitor.visitLeaf(this);
        }
    }

    @Value(staticConstructor = "of")
    private static class Inner<E> implements Node<E> {
        private Node<E> left;
        private Node<E> right;

        @Override
        public <R> R accept(NodeVisitor<E, R> visitor) {
            return visitor.visitInner(this);
        }
    }

    private interface NodeVisitor<E, R> {
        R visitLeaf(Leaf<E> leaf);

        R visitInner(Inner<E> inner);
    }

    @Test
    void testSearch() {
        final Node<String> tree = Inner.of(
            Inner.of(Leaf.of("A"), Leaf.of("B")),
            Inner.of(Leaf.of("C"), Inner.of(Leaf.of("D"), Leaf.of("E"))));

        final Search<Node<String>> bfs = new BreadthFirstSearch<Node<String>>();
        final Collection<List<EgressEdge<Node<String>>>> foundPaths = new ArrayList<>();
        final Graph<Node<String>> graph =
            bfs.search(tree, node -> node.accept(new NodeVisitor<String, List<EgressEdge<Node<String>>>>() {

                @Override
                public List<EgressEdge<Node<String>>> visitLeaf(Leaf<String> leaf) {
                    return Collections.emptyList();
                }

                @Override
                public List<EgressEdge<Node<String>>> visitInner(Inner<String> inner) {
                    return Arrays.asList(
                        EgressEdge.of("left", inner.getLeft()),
                        EgressEdge.of("right", inner.getRight()));
                }
            }), (node, egressEdges) -> node.accept(new NodeVisitor<String, Boolean>() {

                @Override
                public Boolean visitLeaf(Leaf<String> leaf) {
                    return leaf.getValue().equals("D");
                }

                @Override
                public Boolean visitInner(Inner<String> inner) {
                    return false;
                }
            }), (node, egressEdges) -> false, foundPaths);
        System.err.println(graph);
        System.err.println(
            foundPaths.stream()
                .map(path -> path.stream().map(edge -> edge.getLabel()).collect(Collectors.toList()))
                .collect(Collectors.toList()));
    }

}