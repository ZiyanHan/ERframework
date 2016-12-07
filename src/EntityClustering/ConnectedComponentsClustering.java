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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class ConnectedComponentsClustering extends AbstractEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(ConnectedComponentsClustering.class.getName());

    public ConnectedComponentsClustering() {
        super();
        
        LOGGER.log(Level.INFO, "Initializing Connected Components Clustering...");
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeData(simPairs);
        initializeGraph();
        
        // add an edge for every pair of entities with a weight higher than the thrshold
        final Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
                similarityGraph.addEdge(comparison.getEntityId1(), comparison.getEntityId2() + datasetLimit);
            }
        }

        return getConnectedComponents();
    }

    @Override
    public String getMethodInfo() {
        return "Connected Components Clustering: gets duplicate clsuters from the transitive closure of the similarity graph";
    }

    @Override
    public String getMethodParameters() {
        return "The Connected Components Clustering algorithm involves 1 parameter:\n" 
             + explainThresholdParameter();
    }
}
