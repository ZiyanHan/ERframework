package EntityClustering;

import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import DataModel.GomoryHuTree;
import DataModel.SimilarityPairs;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 *
 * @author G.A.P. II
 */
public class CutClustering extends AbstractEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(CutClustering.class.getName());

    protected double Acap;
    protected SimpleWeightedGraph weightedGraph;
    
    public CutClustering() {
        super();
        Acap = 0.3;
        
        LOGGER.log(Level.INFO, "Initializing Cut Clustering...");
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeData(simPairs);
        initializeGraph();

        final Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {	// add an edge for every pair of entities with a weight higher than the threshold
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
            	DefaultWeightedEdge e = (DefaultWeightedEdge) weightedGraph.addEdge(comparison.getEntityId1()+"", (comparison.getEntityId2()+ datasetLimit)+"");
            	weightedGraph.setEdgeWeight(e, comparison.getUtilityMeasure()); 
            }
        }
        
        GomoryHuTree ght = new GomoryHuTree(weightedGraph); //take the minimum cut (Gomory-Hu) tree from the similarity graph
        similarityGraph = ght.MinCutTree();
        similarityGraph.removeVertex(noOfEntities); //remove the artificial sink
        
        return getConnectedComponents();
    }

    @Override
    public String getMethodInfo() {
        return "Cut Clustering : it partitions the similarity graph into equivalence clusters based on its minimum cut.";
    }

    @Override
    public String getMethodParameters() {
        return "The Cut Clustering algorithm involves 2 parameters:\n" 
             + explainThresholdParameter()
             + "2) A-cap : double, default value : 0.3\n"
             + "It determines the weight of the capacity edges, which connect every vertex with the artificial sink.\n";
    }
    
    @Override
    protected void initializeGraph() {
        weightedGraph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        
        String sinkLabel = ""+noOfEntities;
        weightedGraph.addVertex(sinkLabel); //add the artificial sink
        for (int i = 0; i < noOfEntities; i++) {
            String edgeLabel = i+"";
            weightedGraph.addVertex(edgeLabel);
            DefaultWeightedEdge e = (DefaultWeightedEdge) weightedGraph.addEdge(sinkLabel, edgeLabel); // add the capacity edges "a"
            weightedGraph.setEdgeWeight(e, Acap); //connecting the artificial sink with all vertices
        }
        
        LOGGER.log(Level.INFO, "Added {0} nodes in the graph", noOfEntities);
    }
    
    public void setA(double Acap) {
        this.Acap = Acap;
    }
}
