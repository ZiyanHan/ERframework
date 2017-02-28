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

package BlockProcessing.ComparisonRefinement;

import DataModel.AbstractBlock;
import DataModel.Comparison;
import DataModel.DecomposedBlock;
import Utilities.Comparators.ComparisonWeightComparator;
import Utilities.Enumerations.WeightingScheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author gap2
 */

public class CardinalityEdgePruning extends WeightedEdgePruning {

    protected double minimumWeight;
    protected Queue<Comparison> topKEdges;
    
    protected CardinalityEdgePruning(WeightingScheme scheme) {
        super(scheme);
        nodeCentric = false;
    }

    protected void addDecomposedBlock(Collection<Comparison> comparisons, List<AbstractBlock> newBlocks) {
        if (comparisons.isEmpty()) {
            return;
        }

        int[] entityIds1 = new int[comparisons.size()];
        int[] entityIds2 = new int[comparisons.size()];
        double[] similarities = new double[comparisons.size()];
        
        int index = 0;
        Iterator<Comparison> iterator = comparisons.iterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            entityIds1[index] = comparison.getEntityId1();
            entityIds2[index] = comparison.getEntityId2();
            similarities[index] = comparison.getUtilityMeasure();
            index++;
        }

        newBlocks.add(new DecomposedBlock(cleanCleanER, similarities, entityIds1, entityIds2));
    }

    @Override
    public String getMethodInfo() {
        return "Cardinality Edge Pruning: a Meta-blocking method that retains the comparisons "
                + "that correspond to the top-K weighted edges in the blocking graph.";
    }

    @Override
    public String getMethodParameters() {
        return "Cardinality Edge Pruning involves a single parameter:\n"
                + "the weighting scheme that assigns weights to the edges of the blcoking graph.";
    }
    
    @Override
    protected List<AbstractBlock> pruneEdges() {
        minimumWeight = Double.MIN_VALUE;
        topKEdges = new PriorityQueue<Comparison>((int) (2 * threshold), new ComparisonWeightComparator());

        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                verifyValidEntities(i);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                verifyValidEntities(i);
            }
        }

        List<AbstractBlock> newBlocks = new ArrayList<>();
        addDecomposedBlock(topKEdges, newBlocks);
        return newBlocks;
    }

    @Override
    protected void setThreshold() {
        threshold = blockAssignments / 2;
    }

    protected void verifyValidEntities(int entityId) {
        for (int neighborId : validEntities) {
            double weight = getWeight(entityId, neighborId);
            if (weight < minimumWeight) {
                continue;
            }

            Comparison comparison = getComparison(entityId, neighborId);
            comparison.setUtilityMeasure(weight);

            topKEdges.add(comparison);
            if (threshold < topKEdges.size()) {
                Comparison lastComparison = topKEdges.poll();
                minimumWeight = lastComparison.getUtilityMeasure();
            }
        }
    }
}
