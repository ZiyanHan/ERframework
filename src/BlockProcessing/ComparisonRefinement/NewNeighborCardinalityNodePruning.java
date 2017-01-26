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
import Utilities.Comparators.ComparisonWeightComparator;
import Utilities.Enumerations.WeightingScheme;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author gap2
 */
public class NewNeighborCardinalityNodePruning extends CardinalityEdgePruning {

    protected int[][] associatedEntities;

    protected Set<Integer> distinctNeighbors;
    protected Comparison[][] nearestEntities;

    public NewNeighborCardinalityNodePruning(int[][] neighbors, WeightingScheme scheme) {
        super(scheme);
        nodeCentric = true;
        associatedEntities = neighbors;
        distinctNeighbors = new HashSet<>();
    }

    public double getArcsWeight(int entityId1, int entityId2) {
        int[] blocks1 = entityIndex.getEntityBlocks(entityId1, 0);
        int[] blocks2 = entityIndex.getEntityBlocks(entityId2, 0);
        if (blocks1 == null || blocks2 == null) {
            return 0.0;
        }

        AbstractBlock[] blocksArray = null;
        if (cleanCleanER) {
            blocksArray = bBlocks;
        } else {
            blocksArray = uBlocks;
        }

        int i = 0;
        int j = 0;
        double weight = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }
                if (blocks1[i] < blocks2[j]) {
                    break;
                }
                if (blocks1[i] == blocks2[j]) {
                    j++;
                    weight += 1.0 / blocksArray[blocks1[i]].getNoOfComparisons();
                }
            }
            i++;
        }

        return weight;
    }

    public double getCbsWeight(int entityId1, int entityId2) {
        int[] blocks1 = entityIndex.getEntityBlocks(entityId1, 0);
        int[] blocks2 = entityIndex.getEntityBlocks(entityId2, 0);
        if (blocks1 == null || blocks2 == null) {
            return 0.0;
        }

        int i = 0;
        int j = 0;
        double weight = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }
                if (blocks1[i] < blocks2[j]) {
                    break;
                }
                if (blocks1[i] == blocks2[j]) {
                    j++;
                    weight++;
                }
            }
            i++;
        }

        return weight;
    }

    public double getCommonNeighbors(int entityId1, int entityId2) {
        int[] blocks1 = entityIndex.getEntityBlocks(entityId1, 0);
        int[] blocks2 = entityIndex.getEntityBlocks(entityId2, 0);
        if (blocks1 == null || blocks2 == null) {
            return 0.0;
        }

        int i = 0;
        int j = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }
                if (blocks1[i] < blocks2[j]) {
                    break;
                }
                if (blocks1[i] == blocks2[j]) {
                    return 1.0;
                }
            }
            i++;
        }

        return 0.0;
    }

    public double getEcbsWeight(int entityId1, int entityId2) {
        int[] blocks1 = entityIndex.getEntityBlocks(entityId1, 0);
        int[] blocks2 = entityIndex.getEntityBlocks(entityId2, 0);
        if (blocks1 == null || blocks2 == null) {
            return 0.0;
        }

        int i = 0;
        int j = 0;
        double weight = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }
                if (blocks1[i] < blocks2[j]) {
                    break;
                }
                if (blocks1[i] == blocks2[j]) {
                    j++;
                    weight++;
                }
            }
            i++;
        }

        return weight * Math.log10(noOfBlocks / (double) noOfBlocks1) * Math.log10(noOfBlocks / (double) noOfBlocks2);
    }

    public double getEjsWeight(int entityId1, int entityId2) {
        int[] blocks1 = entityIndex.getEntityBlocks(entityId1, 0);
        int[] blocks2 = entityIndex.getEntityBlocks(entityId2, 0);
        if (blocks1 == null || blocks2 == null) {
            return 0.0;
        }

        int i = 0;
        int j = 0;
        double weight = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }
                if (blocks1[i] < blocks2[j]) {
                    break;
                }
                if (blocks1[i] == blocks2[j]) {
                    j++;
                    weight++;
                }
            }
            i++;
        }

        double js = weight / (noOfBlocks1 + noOfBlocks2 - weight);
        return js * Math.log10(distinctComparisons / comparisonsPerEntity[entityId1]) * Math.log10(distinctComparisons / comparisonsPerEntity[entityId2]);
    }

    public double getJsWeight(int entityId1, int entityId2) {
        int[] blocks1 = entityIndex.getEntityBlocks(entityId1, 0);
        int[] blocks2 = entityIndex.getEntityBlocks(entityId2, 0);
        if (blocks1 == null || blocks2 == null) {
            return 0.0;
        }

        int i = 0;
        int j = 0;
        double weight = 0;
        int noOfBlocks1 = blocks1.length;
        int noOfBlocks2 = blocks2.length;
        while (i < noOfBlocks1) {
            while (j < noOfBlocks2) {
                if (blocks2[j] < blocks1[i]) {
                    j++;
                    continue;
                }
                if (blocks1[i] < blocks2[j]) {
                    break;
                }
                if (blocks1[i] == blocks2[j]) {
                    j++;
                    weight++;
                }
            }
            i++;
        }

        return weight / (noOfBlocks1 + noOfBlocks2 - weight);
    }

    @Override
    public String getMethodInfo() {
        return "Cardinality Node Pruning: a Meta-blocking method that retains for every entity, "
                + "the comparisons that correspond to its top-k weighted edges in the blocking graph.";
    }

    @Override
    public String getMethodParameters() {
        return "Cardinality Node Pruning involves a single parameter:\n"
                + "the weighting scheme that assigns weights to the edges of the blcoking graph.";
    }

    @Override
    protected double getWeight(int entityId1, int entityId2) {
        if (weightingScheme == null) {
            return getCommonNeighbors(entityId1, entityId2);
        }

        switch (weightingScheme) {
            case ARCS:
                return getArcsWeight(entityId1, entityId2);
            case CBS:
                return getCbsWeight(entityId1, entityId2);
            case ECBS:
                return getEcbsWeight(entityId1, entityId2);
            case JS:
                return getJsWeight(entityId1, entityId2);
            case EJS:
                return getEjsWeight(entityId1, entityId2);
        }

        return -1;
    }

    @Override
    protected List<AbstractBlock> pruneEdges() {
        nearestEntities = new Comparison[noOfEntities][];
        topKEdges = new PriorityQueue<Comparison>((int) (2 * threshold), new ComparisonWeightComparator());
        for (int entityId = 0; entityId < noOfEntities; entityId++) {
            topKEdges.clear();
            validEntities.clear();
            distinctNeighbors.clear();
            minimumWeight = 0;

            final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0); //entity index
            if (associatedBlocks.length == 0) {
                continue;
            }

            for (int blockIndex : associatedBlocks) {
                setNormalizedNeighborEntities(blockIndex, entityId);
                distinctNeighbors.addAll(neighbors);
            }

            int[] entityAssociates = associatedEntities[entityId]; //rdf neighbors
            if (entityAssociates == null) {
                continue;
            }

            for (int neighborId : distinctNeighbors) { //for each candidate match of this entity
                double degreeOfCooccurrence = 0;
                int[] neighborAssociates = associatedEntities[neighborId];
                if (neighborAssociates == null) {
                    continue;
                }

                for (int associateId : entityAssociates) { //for each rdf neighbor of the current entity
                    for (int nAssociateId : neighborAssociates) { //for each rdf neighbor of the candidate match
                        degreeOfCooccurrence += getWeight(associateId, nAssociateId);
                    }
                }

                if (degreeOfCooccurrence <= minimumWeight) {
                    continue;
                }

                Comparison comparison = getComparison(entityId, neighborId);
                comparison.setUtilityMeasure(degreeOfCooccurrence);

                topKEdges.add(comparison);
                if (threshold < topKEdges.size()) {
                    Comparison lastComparison = topKEdges.poll();
                    minimumWeight = lastComparison.getUtilityMeasure();
                }
            }

            int comparisonIndex = topKEdges.size();
            nearestEntities[entityId] = new Comparison[comparisonIndex];
            while (0 < comparisonIndex) {
                nearestEntities[entityId][comparisonIndex] = topKEdges.poll();
                comparisonIndex--;
            }
        }

        return null;
    }

    @Override
    protected void setThreshold() {
        threshold = Math.max(1, blockAssingments / noOfEntities);
    }
}
