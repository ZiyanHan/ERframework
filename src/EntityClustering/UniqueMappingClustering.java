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
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vefthym
 */
public class UniqueMappingClustering extends AbstractEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(UniqueMappingClustering.class.getName());

    private Set<Integer> matchedIds; //the ids of entities that have been already matched
    
    public UniqueMappingClustering() {
        super();
        matchedIds = new HashSet<>();
        
        LOGGER.log(Level.INFO, "Initializing Unique Mapping Clustering...");
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        if (simPairs.getNoOfComparisons() == 0) {
            return new ArrayList<>();
        }
        initializeData(simPairs);
        initializeGraph();
        final Queue<SimilarityEdge> SEqueue = new PriorityQueue<>(simPairs.getNoOfComparisons(), new SimilarityEdgeComparator());

        final Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) { // add a similarity edge to the queue, for every pair of entities with a weight higher than the threshold
            Comparison comparison = iterator.next();            
            if (threshold < comparison.getUtilityMeasure()) {
                SEqueue.add(new SimilarityEdge(comparison.getEntityId1(), comparison.getEntityId2()+datasetLimit, comparison.getUtilityMeasure()));
            }
        }

        System.out.println("Similarity Pairs ordered:");
        while (!SEqueue.isEmpty()) {
            SimilarityEdge se = SEqueue.remove();            
            int e1 = se.getModel1Pos();
            int e2 = se.getModel2Pos();

//            System.out.print(se);
                       
            //skip already matched entities (unique mapping contraint for clean-clean ER)
            if (matchedIds.contains(e1) || matchedIds.contains(e2)) {
//                System.out.println("\t-->\tSkipped!");
                continue;
            }
            
//            System.out.println("\t-->\tMatched!");
            similarityGraph.addEdge(e1, e2);
            matchedIds.add(e1);
            matchedIds.add(e2);
        }
        
        return getConnectedComponents();
    }

    @Override
    public String getMethodInfo() {
        return "Unique Mapping Clustering: Create a cluster for each pair of entities, none of which has been matched previously. ";
    }

    @Override
    public String getMethodParameters() {
        return "The Unique Mapping Clustering algorithm involves 1 parameter:\n" 
             + explainThresholdParameter();
    }
}
