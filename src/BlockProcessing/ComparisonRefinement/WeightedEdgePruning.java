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

import Utilities.DataStructures.AbstractDuplicatePropagation;
import DataModel.AbstractBlock;
import DataModel.BilateralBlock;
import Utilities.Enumerations.WeightingScheme;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */
public class WeightedEdgePruning extends AbstractMetablocking {

    protected double noOfEdges;

    public WeightedEdgePruning(WeightingScheme scheme) {
        super(scheme);
        nodeCentric = false;
    }

    @Override
    public void deduplicateBlocks(AbstractDuplicatePropagation adp, List<AbstractBlock> blocks) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMethodInfo() {
        return "Weighted Edge Pruning: a Meta-blocking method that retains all comparisons "
                + "that have a weight higher than the average edge weight in the blocking graph.";
    }

    @Override
    public String getMethodParameters() {
        return "Weighted Edge Pruning involves a single parameter:\n"
                + "the weighting scheme that assigns weights to the edges of the blcoking graph.";
    }
    
    protected void processArcsEntity(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            double blockComparisons = cleanCleanER ? bBlocks[blockIndex].getNoOfComparisons() : uBlocks[blockIndex].getNoOfComparisons();
            setNormalizedNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (flags[neighborId] != entityId) {
                    counters[neighborId] = 0;
                    flags[neighborId] = entityId;
                }

                counters[neighborId] += 1 / blockComparisons;
                validEntities.add(neighborId);
            }
        }
    }
    
    protected void processWjsEntity(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }
        if (!cleanCleanER) {
            throw new UnsupportedOperationException("WJS weighting not supported for Dirty ER yet...");
        }

        for (int blockIndex : associatedBlocks) {            
            BilateralBlock block = bBlocks[blockIndex];      
            int[] d1EntitiesInBlock = block.getIndex1Entities();
            int[] d2EntitiesInBlock = block.getIndex2Entities();
            double weight1 = 0, weight2 = 0;
            if (d1EntitiesInBlock != null) {
                weight1 = Math.log10((double)datasetLimit/block.getIndex1Entities().length);
            }
            if (d2EntitiesInBlock != null) {
                weight2 = Math.log10((double)(noOfEntities-datasetLimit)/block.getIndex2Entities().length);
            }
            
            setNormalizedNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (flags[neighborId] != entityId) {
                    counters[neighborId] = 0;
                    flags[neighborId] = entityId;
                }

                counters[neighborId] += weight1 + weight2; //this creates only the numerator
                validEntities.add(neighborId);
            }
        }
    }

    protected void processEntity(int entityId) {
        validEntities.clear();
        final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
        if (associatedBlocks.length == 0) {
            return;
        }

        for (int blockIndex : associatedBlocks) {
            setNormalizedNeighborEntities(blockIndex, entityId);
            for (int neighborId : neighbors) {
                if (flags[neighborId] != entityId) {
                    counters[neighborId] = 0;
                    flags[neighborId] = entityId;
                }

                counters[neighborId]++;
                validEntities.add(neighborId);
            }
        }
    }

    @Override
    protected List<AbstractBlock> pruneEdges() {
        List<AbstractBlock> newBlocks = new ArrayList<>();
        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        } else if (weightingScheme.equals(WeightingScheme.WJS)) {
            for (int i = 0; i < limit; i++) {
                processWjsEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                verifyValidEntities(i, newBlocks);
            }
        }
        return newBlocks;
    }

    @Override
    protected void setThreshold() {
        noOfEdges = 0;
        threshold = 0;

        int limit = cleanCleanER ? datasetLimit : noOfEntities;
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = 0; i < limit; i++) {
                processArcsEntity(i);
                updateThreshold(i);
            }
        } else {
            for (int i = 0; i < limit; i++) {
                processEntity(i);
                updateThreshold(i);
            }
        }

        threshold /= noOfEdges;
    }

    protected void updateThreshold(int entityId) {
        noOfEdges += validEntities.size();
        for (int neighborId : validEntities) {
            threshold += getWeight(entityId, neighborId);
        }
    }

    protected void verifyValidEntities(int entityId, List<AbstractBlock> newBlocks) {
        retainedNeighbors.clear();
        if (!cleanCleanER) {
            for (int neighborId : validEntities) {
                double weight = getWeight(entityId, neighborId);
                if (threshold <= weight) {
                    retainedNeighbors.add(neighborId);
                }
            }
            addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
        } else {
            if (entityId < datasetLimit) {
                for (int neighborId : validEntities) {
                    double weight = getWeight(entityId, neighborId);
                    if (threshold <= weight) {
                        retainedNeighbors.add(neighborId-datasetLimit);
                    }
                }
                addDecomposedBlock(entityId, retainedNeighbors, newBlocks);
            } else {
                for (int neighborId : validEntities) {
                    double weight = getWeight(entityId, neighborId);
                    if (threshold <= weight) {
                        retainedNeighbors.add(neighborId);
                    }
                }
                addReversedDecomposedBlock(entityId-datasetLimit, retainedNeighbors, newBlocks);
            }
        }
    }
}
