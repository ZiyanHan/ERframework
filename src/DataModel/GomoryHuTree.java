package DataModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import org.jgrapht.*;
import org.jgrapht.alg.MinSourceSinkCut;
import org.jgrapht.graph.*;

public class GomoryHuTree<V, E> {

    private final SimpleWeightedGraph<V, E> graph;
    // the bogus graph for
    // private final SimpleWeightedGraph<V, DefaultWeightedEdge> returnGraph;
    public GomoryHuTree(SimpleWeightedGraph<V, E> graph){
        this.graph = graph;
        this.graph.getEdgeFactory();
    }

    private DefaultDirectedWeightedGraph<V, DefaultWeightedEdge>
        makeDirectedCopy(UndirectedGraph<V, E> graph)
    {
        DefaultDirectedWeightedGraph<V, DefaultWeightedEdge> copy =
            new DefaultDirectedWeightedGraph<>(
                DefaultWeightedEdge.class);

        Graphs.addAllVertices(copy, graph.vertexSet());
        for (E e : this.graph.edgeSet()) {
            V v1 = this.graph.getEdgeSource(e);
            V v2 = this.graph.getEdgeTarget(e);
            Graphs.addEdge(copy, v1, v2, graph.getEdgeWeight(e));
            Graphs.addEdge(copy, v2, v1, graph.getEdgeWeight(e));
        }
        return copy;
    }

    public SimpleGraph<Integer, DefaultEdge> MinCutTree(){
        HashMap<V, V> predecessors = new HashMap<>();
        DefaultDirectedWeightedGraph<V, DefaultWeightedEdge> directedGraph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultDirectedWeightedGraph<V, DefaultWeightedEdge> returnGraphClone =
                new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        SimpleGraph<Integer, DefaultEdge> returnGraph= new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        directedGraph = this.makeDirectedCopy(this.graph);
        MinSourceSinkCut<V, DefaultWeightedEdge> minSourceSinkCut = new MinSourceSinkCut<V, DefaultWeightedEdge>(directedGraph);
        Set<V> vertexSet = directedGraph.vertexSet();
        Iterator<V> it = vertexSet.iterator();
        V start = it.next();
        predecessors.put(start, start);
        while(it.hasNext()){
            V vertex = it.next();
            predecessors.put(vertex, start);
        }

        Iterator<V> itVertices = directedGraph.vertexSet().iterator();
        itVertices.next();
        while(itVertices.hasNext()){
            V vertex = itVertices.next();
            V predecessor = predecessors.get(vertex);
            minSourceSinkCut.computeMinCut(vertex, predecessor);
//            System.out.println(vertex + " " + predecessor);
            returnGraphClone.addVertex(vertex);
            returnGraphClone.addVertex(predecessor);
            returnGraph.addVertex(Integer.parseInt(vertex+""));
            returnGraph.addVertex(Integer.parseInt(predecessor+""));
            Set<V> sourcePartition = minSourceSinkCut.getSourcePartition();
            double flowValue = minSourceSinkCut.getCutWeight();
            DefaultWeightedEdge e = (DefaultWeightedEdge) returnGraphClone.addEdge(vertex, predecessor);
            returnGraph.addEdge(Integer.parseInt(vertex+""), Integer.parseInt(predecessor+""));
            returnGraphClone.setEdgeWeight(e, flowValue); 
//            System.out.println(vertex+" "+predecessor+" "+flowValue);
//            System.out.println(sourcePartition + " " +
//                    minSourceSinkCut.getSinkPartition());
            for(V sourceVertex : this.graph.vertexSet()){
                if(predecessors.get(sourceVertex).equals(predecessor)
                        && sourcePartition.contains(sourceVertex)){
                    predecessors.put(sourceVertex, vertex);
                }
            }
        }
        return returnGraph;
        
    }


}