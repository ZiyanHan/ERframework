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
import DataModel.SimilarityEdge;
import DataModel.SimilarityPairs;
import Utilities.Comparators.SimilarityEdgeComparator;
import Utilities.TextModels.AbstractModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
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
public class MergeCenterClustering implements IEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(MergeCenterClustering.class.getName());

    private int noOfEntities;
    private int datasetLimit;
    private final SimpleGraph similarityGraph;

    public MergeCenterClustering() {
        similarityGraph = new SimpleGraph(DefaultEdge.class);
        LOGGER.log(Level.INFO, "Initializing Connected Components clustering...");
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeGraph(simPairs);
        SimilarityEdgeComparator SEcomparator = new SimilarityEdgeComparator();
        PriorityQueue<SimilarityEdge> SEqueue = 
            new PriorityQueue<SimilarityEdge>(simPairs.getNoOfComparisons(), SEcomparator);
        // add an edge for every pair of entities with a weight higher than the thrshold
        double threshold = getSimilarityThreshold(simPairs);
        Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
                SimilarityEdge se = new SimilarityEdge (comparison.getEntityId1(), (comparison.getEntityId2()+ datasetLimit), comparison.getUtilityMeasure());
                SEqueue.add(se);
            }
        }
        
        Set<Integer> Center =  new HashSet<Integer>();
        Set<Integer> NonCenter = new HashSet<Integer>();
        while (SEqueue.size() > 0)
        {
            SimilarityEdge se = SEqueue.remove();
            int v1 = se.getModel1Pos();
            int v2 = se.getModel2Pos();
            double sim = se.getSimilarity();

            if (!(Center.contains(v1)||Center.contains(v2)||NonCenter.contains(v1)||NonCenter.contains(v2)))
            {
                Center.add(v1);
                NonCenter.add(v2);
                similarityGraph.addEdge(v1, v2);
            }
            else if ((Center.contains(v1)&&Center.contains(v2))||(NonCenter.contains(v1)&&NonCenter.contains(v2)))
            {
                continue;
            }
            else if (Center.contains(v1))
            {
                NonCenter.add(v2);
                similarityGraph.addEdge(v1, v2);
            }
            else if (Center.contains(v2))
            {
                NonCenter.add(v1);
                similarityGraph.addEdge(v1, v2);
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

        double threshold = 0.5;

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
