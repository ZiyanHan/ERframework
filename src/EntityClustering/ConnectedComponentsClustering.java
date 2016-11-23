/*
* Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package EntityClustering;

import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author G.A.P. II
 */
public class ConnectedComponentsClustering implements IEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(ConnectedComponentsClustering.class.getName());

    private int noOfEntities;
    private int datasetLimit;
    private final SimpleGraph similarityGraph;

    public ConnectedComponentsClustering() {
        similarityGraph = new SimpleGraph(DefaultEdge.class);
        LOGGER.log(Level.INFO, "Initializing Connected Components clustering...");
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeGraph(simPairs);

        // add an edge for every pair of entities with a weight higher than the thrshold
        double threshold = getSimilarityThreshold(simPairs);
        Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
                similarityGraph.addEdge(comparison.getEntityId1(), comparison.getEntityId2() + datasetLimit);
            }
        }

        // get connected components
        ConnectivityInspector ci = new ConnectivityInspector(similarityGraph);
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
        
        double threshold = averageSimilarity + 3*standardDeviation;
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

        for (int i = 0; i < noOfEntities; i++) {
            similarityGraph.addVertex(i);
        }
        LOGGER.log(Level.INFO, "Added {0} nodes in the graph", noOfEntities);
    }
}
