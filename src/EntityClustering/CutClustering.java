package EntityClustering;

import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import DataModel.GomoryHuTree;
import DataModel.SimilarityPairs;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 *
 * @author G.A.P. II
 */
public class CutClustering implements IEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(CutClustering.class.getName());

    private int noOfEntities;
    private int datasetLimit;
    private double Acap = 0.3;
    private final SimpleWeightedGraph<String, DefaultWeightedEdge> similarityGraph;

    public CutClustering() {
        similarityGraph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        LOGGER.log(Level.INFO, "Initializing Connected Components clustering...");
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeGraph(simPairs);
        double threshold = getSimilarityThreshold(simPairs);
        DefaultWeightedEdge e;
        Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {	// add an edge for every pair of entities with a weight higher than the threshold
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
            	e = (DefaultWeightedEdge) similarityGraph.addEdge(comparison.getEntityId1()+"", (comparison.getEntityId2()+ datasetLimit)+"");
            	similarityGraph.setEdgeWeight(e, comparison.getUtilityMeasure()); 
            }
        }
        
        GomoryHuTree ght = new GomoryHuTree(similarityGraph); //take the minimum cut (Gomory-Hu) tree from the similarity graph
        SimpleGraph<Integer, DefaultEdge>  minCutTree = ght.MinCutTree();
        minCutTree.removeVertex(noOfEntities); //remove the artificial sink
        // get connected components
        ConnectivityInspector ci = new ConnectivityInspector(minCutTree);
        List<Set<Integer>> connectedComponents = ci.connectedSets();

        // prepare output
        List<EquivalenceCluster> equivalenceClusters = new ArrayList<>();
        for (Set<Integer> componentIds : connectedComponents) {
            EquivalenceCluster newCluster = new EquivalenceCluster();
            equivalenceClusters.add(newCluster);
            if (!simPairs.isCleanCleanER()) {
                newCluster.loadBulkEntityIdsD1(componentIds);
                continue;
            }

            for (Integer entityId : componentIds) {
                if (entityId < datasetLimit) {
                    newCluster.addEntityIdD1(entityId);
                } else {
                    newCluster.addEntityIdD2(entityId-datasetLimit);
                }
            }
        }
        return equivalenceClusters;
    }

    public int getMaxEntityId(int[] entityIds) {
        int maxId = Integer.MIN_VALUE;
        for (int i = 0; i < entityIds.length; i++) {
            if (maxId < entityIds[i]) {
                maxId = entityIds[i];
            }
        }
        return maxId;
    }

    @Override
    public String getMethodInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMethodParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private double getSimilarityThreshold(SimilarityPairs simPairs) {
        double averageSimilarity = 0;
        Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            averageSimilarity += comparison.getUtilityMeasure();

        }
        averageSimilarity /= simPairs.getNoOfComparisons();

        double standardDeviation = 0;
        iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            standardDeviation += Math.pow(comparison.getUtilityMeasure()-averageSimilarity, 2.0);
        }
        standardDeviation = Math.sqrt(standardDeviation/simPairs.getNoOfComparisons());

        double threshold = 0.2;

        LOGGER.log(Level.INFO, "Similarity threshold : {0}", threshold);
        return threshold;
    }
    
    
    private void initializeGraph(SimilarityPairs simPairs) {
        int maxEntity1 = getMaxEntityId(simPairs.getEntityIds1());
        int maxEntity2 = getMaxEntityId(simPairs.getEntityIds2());
        if (simPairs.isCleanCleanER()) {
            datasetLimit = maxEntity1 + 1;
            noOfEntities = maxEntity1 + maxEntity2 + 2;
        } else {
            datasetLimit = 0;
            noOfEntities = Math.max(maxEntity1, maxEntity2) + 1;
        }
        similarityGraph.addVertex(""+noOfEntities); //add the artificial sink
        DefaultWeightedEdge e;
        
        for (int i = 0; i < noOfEntities; i++) {
            similarityGraph.addVertex(i+"");
            e = (DefaultWeightedEdge) similarityGraph.addEdge(""+noOfEntities, i+""); // add the capacity edges "a"
            similarityGraph.setEdgeWeight(e, Acap); //connecting the artificial sink with all vertices
        }
        LOGGER.log(Level.INFO, "Added {0} nodes in the graph", noOfEntities);
    }
    
    public void setA(double Acap) {
        this.Acap = Acap;
    }
}
