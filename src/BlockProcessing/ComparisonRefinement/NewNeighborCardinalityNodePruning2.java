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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.primitives.Ints;

/**
 *
 * @author gap2
 */
public class NewNeighborCardinalityNodePruning2 extends CardinalityEdgePruning {

    protected int[][] rdfOutNeighbors;
    protected int[][] rdfInNeighbors;

    protected Set<Integer> distinctBlockingGraphNeighbors;
    protected Comparison[][] nearestEntities;

    public NewNeighborCardinalityNodePruning2(int[][] outNeighbors, WeightingScheme scheme) {
        super(scheme);
        nodeCentric = true;
        rdfOutNeighbors = outNeighbors;
        distinctBlockingGraphNeighbors = new HashSet<>();
        rdfInNeighbors = getInNeighborsFromOutNeighbors(outNeighbors);
    }
    
    private int[][] getInNeighborsFromOutNeighbors(int[][] rdfOutNeighbors) {
        Multimap<Integer,Integer> inNeighbors = HashMultimap.create();        
        for (int i = 0; i < rdfOutNeighbors.length; ++i) {
            for (int j = 0; j < rdfOutNeighbors[i].length; ++j) {
                inNeighbors.put(rdfOutNeighbors[i][j], i);
            }
        }
        rdfInNeighbors = new int[noOfEntities][];
        for (int i = 0; i < inNeighbors.keySet().size(); ++i) {
            Collection<Integer> tmp  = inNeighbors.get(i);            
            if (tmp != null) {                
                rdfInNeighbors[i] = tmp.stream().mapToInt(j->j).toArray(); //alternative
//                rdfInNeighbors[i] = Ints.toArray(tmp); //alternative
                
            }   
        }
        return rdfInNeighbors;
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
        topKEdges = new PriorityQueue<>((int) (2 * threshold), new ComparisonWeightComparator());
        for (int entityId = 0; entityId < noOfEntities; entityId++) {
            topKEdges.clear();
            validEntities.clear();
            distinctBlockingGraphNeighbors.clear();
            minimumWeight = 0;

            final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0); //entity index
            if (associatedBlocks.length == 0) {
                continue;
            }

            for (int blockIndex : associatedBlocks) {
                setNormalizedNeighborEntities(blockIndex, entityId);
                distinctBlockingGraphNeighbors.addAll(neighbors); //entities appearing in a common block with this entity (candidate matches)
            }

            int[] outNeighbors = rdfOutNeighbors[entityId]; //rdf neighbors
            if (outNeighbors == null) {
                continue;
            }
            
            double maxSim = 0;

            for (int outNeighbor : outNeighbors) { //for each rdf out-neighbor of this entity
                double degreeOfCooccurrence = 0;
                
                Set<Integer> candidateMatchesOfOutNeighbor = new HashSet<>();
                for (int blockIndex : entityIndex.getEntityBlocks(outNeighbor, 0)) {
                    List<Integer> neighbors = getNormalizedNeighborEntities(blockIndex, outNeighbor); //on-purpose shadowing
                    candidateMatchesOfOutNeighbor.addAll(neighbors); //entities appearing in a common block with this out-neighbor (candidate matches of the out-neighbor)
                }
                
                
                for (int candidateMatchOfOutNeighbor : candidateMatchesOfOutNeighbor) { //for each CMMON (call this candidateMatchOfOutNeighbor)
                    degreeOfCooccurrence += getWeight(outNeighbor, candidateMatchOfOutNeighbor);
                    int[] inNeighborsOfCMMON = rdfInNeighbors[candidateMatchOfOutNeighbor]; //get the rdf in-neighbors of CMMON
                    if (inNeighborsOfCMMON != null) {
                        for (int inNeighborOfCMMON : inNeighborsOfCMMON) { //for each rdfInNeighbor of CMMON
                            //TODO: continue from here
                        }
                    }
                    
                }
                //////////////////////////////////////////////////////////////
                //the following are kept from previous version, change them //
                //////////////////////////////////////////////////////////////

                if (degreeOfCooccurrence <= minimumWeight) {
                    continue;
                }

                Comparison comparison = getComparison(entityId, outNeighbor);
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
                comparisonIndex--;
                nearestEntities[entityId][comparisonIndex] = topKEdges.poll();
            }
        }

        return null;
    }

    @Override
    protected void setThreshold() {
        threshold = Math.max(1, blockAssignments / noOfEntities);
    }
    
    public Comparison[][] getNearestEntities() {
        return nearestEntities;
    }
}
