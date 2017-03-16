package NewApproaches.MulticoreProcessing;

import BlockBuilding.StandardBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.BilateralBlock;
import DataModel.Comparison;
import DataModel.IdDuplicates;
import DataModel.UnilateralBlock;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import NewApproaches.Preprocessing;
import Utilities.BlocksPerformance;
import Utilities.Comparators.ComparisonWeightComparator;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class RandomParallelization extends CardinalityNodePruning {

    protected int entityCounter;
    protected int noOfNodes;

    protected Integer[] sortedEntities;
    protected RandomThread[] threads;
    protected Queue<Comparison>[] globallyNearestEntities;
    protected Set<Comparison> totalComparisons;

    public RandomParallelization(int nodes, WeightingScheme wScheme) {
        super(wScheme);
        noOfNodes = nodes;
        nodeCentric = true;
        threads = new RandomThread[noOfNodes];
    }

    @Override
    protected List<AbstractBlock> applyMainProcessing() {
        blockAssignments = 0;
        if (cleanCleanER) {
            for (BilateralBlock bBlock : bBlocks) {
                blockAssignments += bBlock.getTotalBlockAssignments();
            }
        } else {
            for (UnilateralBlock uBlock : uBlocks) {
                blockAssignments += uBlock.getTotalBlockAssignments();
            }
        }

        setLimits();
        setThreshold();
        sortEntities();

        if (weightingScheme.equals(WeightingScheme.EJS)) {
            setStatisticsInParallel();
        }

        entityCounter = 0;
        for (int i = 0; i < noOfNodes; i++) {
            threads[i] = new RandomThread(false);
            threads[i].start();
        }

        for (int i = 0; i < noOfNodes; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        globallyNearestEntities = threads[0].getLocallyNearestEntities();
        for (int i = 1; i < noOfNodes; i++) {
            Queue[] locallyNearestEntities = threads[i].getLocallyNearestEntities();
            for (int j = 0; j < noOfEntities; j++) {
                globallyNearestEntities[j].addAll(locallyNearestEntities[j]);
                while (threshold < globallyNearestEntities[j].size()) {
                    globallyNearestEntities[j].poll();
                }
            }
        }

        totalComparisons = new HashSet<>();
        for (Queue currentNE : globallyNearestEntities) {
            totalComparisons.addAll(currentNE);
        }

        List<AbstractBlock> newBlocks = new ArrayList<>();
        addDecomposedBlock(totalComparisons, newBlocks);
        System.out.println("Total comparisons\t:\t" + totalComparisons.size());
        return newBlocks;
    }

    protected synchronized int getNextEntityId() {
        if (entityCounter < sortedEntities.length) {
            return sortedEntities[entityCounter++];
        }
        return -1;
    }

    private void mergeComparisons() {
        comparisonsPerEntity = new double[noOfEntities];
        for (int i = 0; i < noOfNodes; i++) {
            double[] entityComparisons = threads[i].getEntityComparisons();
            for (int j = 0; j < noOfEntities; j++) {
                comparisonsPerEntity[j] += entityComparisons[j];
            }
        }

        distinctComparisons = 0;
        for (int i = 0; i < noOfEntities; i++) {
            distinctComparisons += comparisonsPerEntity[i];
        }
        distinctComparisons /= 2;
    }

    protected void setStatisticsInParallel() {
        entityCounter = 0;
        for (int i = 0; i < noOfNodes; i++) {
            threads[i] = new RandomThread(true);
            threads[i].start();
        }

        for (int i = 0; i < noOfNodes; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        mergeComparisons();
    }

    protected void sortEntities() {
        final List<Integer> entityIds = new ArrayList<>();
        if (cleanCleanER) {
            for (int i = 0; i < datasetLimit; i++) {
                if (0 < entityIndex.getNoOfEntityBlocks(i, 0)) {
                    entityIds.add(i);
                }
            }
        } else {
            for (int i = 0; i < noOfEntities; i++) {
                if (0 < entityIndex.getNoOfEntityBlocks(i, 0)) {
                    entityIds.add(i);
                }
            }
        }

        Collections.shuffle(entityIds);
        sortedEntities = entityIds.toArray(new Integer[entityIds.size()]);
    }

    class RandomThread extends Thread {

        protected final boolean isEjsFirstRound;
        protected final double[] counters;
        protected double[] entityComparisons;
        protected final int[] flags;
        protected double[] minimumWeights;

        protected final List<Integer> neighbors;
        protected final List<Integer> retainedEntities;
        protected Queue<Comparison>[] locallyNearestEntities;
        protected final Set<Integer> validEntities;

        public RandomThread(boolean ejsFirstRound) {
            isEjsFirstRound = ejsFirstRound;
            if (isEjsFirstRound) {
                entityComparisons = new double[noOfEntities];
            } else {
                minimumWeights = new double[noOfEntities];
                locallyNearestEntities = new PriorityQueue[noOfEntities];
                for (int i = 0; i < noOfEntities; i++) {
                    locallyNearestEntities[i] = new PriorityQueue<Comparison>((int) (2 * threshold), new ComparisonWeightComparator());
                }
            }

            counters = new double[noOfEntities];
            flags = new int[noOfEntities];
            for (int i = 0; i < noOfEntities; i++) {
                flags[i] = -1;
            }

            neighbors = new ArrayList<>();
            retainedEntities = new ArrayList<>();
            validEntities = new HashSet<>();
        }

        public double[] getEntityComparisons() {
            return entityComparisons;
        }

        protected Queue<Comparison>[] getLocallyNearestEntities() {
            return locallyNearestEntities;
        }

        protected double getWeight(int entityId, int neighborId) {
            switch (weightingScheme) {
                case ARCS:
                    return counters[neighborId];
                case CBS:
                    return counters[neighborId];
                case ECBS:
                    return counters[neighborId] * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(entityId, 0)) * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(neighborId, 0));
                case JS:
                    return counters[neighborId] / (entityIndex.getNoOfEntityBlocks(entityId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
                case EJS:
                    double probability = counters[neighborId] / (entityIndex.getNoOfEntityBlocks(entityId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
                    return probability * Math.log10(distinctComparisons / comparisonsPerEntity[entityId]) * Math.log10(distinctComparisons / comparisonsPerEntity[neighborId]);
            }
            return -1;
        }

        protected void processArcsEntity(int entityId) {
            validEntities.clear();
            final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
            for (int blockIndex : associatedBlocks) {
                double blockComparisons = cleanCleanER ? bBlocks[blockIndex].getNoOfComparisons() : uBlocks[blockIndex].getNoOfComparisons();
                List<Integer> currentNeighbors = setNormalizedNeighborEntities(blockIndex, entityId);
                for (int neighborId : currentNeighbors) {
                    if (flags[neighborId] != entityId) {
                        counters[neighborId] = 0;
                        flags[neighborId] = entityId;
                    }

                    counters[neighborId] += 1 / blockComparisons;
                    validEntities.add(neighborId);
                }
            }
        }

        protected void processEjsEntity(int entityId) {
            validEntities.clear();
            final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
            for (int blockIndex : associatedBlocks) {
                List<Integer> currentNeighbors = setNormalizedNeighborEntities(blockIndex, entityId);
                validEntities.addAll(currentNeighbors);
            }
            entityComparisons[entityId] = validEntities.size();

            if (cleanCleanER) {
                //count the number of comparisons for the second dataset, as well
                for (int neighborId : validEntities) {
                    entityComparisons[neighborId]++;
                }
            }
        }

        protected void processEntity(int entityId) {
            validEntities.clear();
            final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
            for (int blockIndex : associatedBlocks) {
                List<Integer> currentNeighbors = setNormalizedNeighborEntities(blockIndex, entityId);
                for (int neighborId : currentNeighbors) {
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
        public void run() {
            int currentId = -1;
            if (isEjsFirstRound) {
                while (0 <= (currentId = getNextEntityId())) {
                    processEjsEntity(currentId);
                }
            } else {
                while (0 <= (currentId = getNextEntityId())) {
                    if (weightingScheme.equals(WeightingScheme.ARCS)) {
                        processArcsEntity(currentId);
                    } else {
                        processEntity(currentId);
                    }
                    verifyValidEntities(currentId);
                }
            }
        }

        protected List<Integer> setNormalizedNeighborEntities(int blockIndex, int entityId) {
            neighbors.clear();
            if (cleanCleanER) {
                if (entityId < datasetLimit) {
                    for (int originalId : bBlocks[blockIndex].getIndex2Entities()) {
                        neighbors.add(originalId + datasetLimit);
                    }
                } else {
                    for (int originalId : bBlocks[blockIndex].getIndex1Entities()) {
                        neighbors.add(originalId);
                    }
                }
            } else if (!nodeCentric) {
                for (int neighborId : uBlocks[blockIndex].getEntities()) {
                    if (neighborId < entityId) {
                        neighbors.add(neighborId);
                    }
                }
            } else {
                for (int neighborId : uBlocks[blockIndex].getEntities()) {
                    if (neighborId != entityId) {
                        neighbors.add(neighborId);
                    }
                }
            }
            return neighbors;
        }

        protected void verifyValidEntities(int entityId) {
            for (int neighborId : validEntities) {
                double weight = getWeight(entityId, neighborId);
                Comparison comparison = getComparison(entityId, neighborId);
                comparison.setUtilityMeasure(weight);

                if (minimumWeights[entityId] < weight) {
                    locallyNearestEntities[entityId].add(comparison);
                    if (threshold < locallyNearestEntities[entityId].size()) {
                        Comparison lastComparison = locallyNearestEntities[entityId].poll();
                        minimumWeights[entityId] = lastComparison.getUtilityMeasure();
                    }
                }

                if (cleanCleanER) {
                    if (minimumWeights[neighborId] < weight) {
                        locallyNearestEntities[neighborId].add(comparison);
                        if (threshold < locallyNearestEntities[neighborId].size()) {
                            Comparison lastComparison = locallyNearestEntities[neighborId].poll();
                            minimumWeights[neighborId] = lastComparison.getUtilityMeasure();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        String mainDirectory = "/home/gpapadakis/newData/";
        String[] datasetsPaths = {mainDirectory + "restaurant/",
            mainDirectory + "rexa_dblp/",
            mainDirectory + "yago_imdb/",
            mainDirectory + "bbcMusic/"
        };

        String[] d1Datasets = {"restaurant1Profiles",
            "rexaProfiles",
            "yagoProfiles",
            "bbc-musicNewNoRdfProfiles"
        };

        String[] d2Datasets = {"restaurant2Profiles",
            "swetodblp_april_2008Profiles",
            "imdbProfiles",
            "dbpedia37NewNoSameAsNoWikipediaProfiles"
        };

        String[] duplicates = {
            "restaurantIdDuplicates",
            "rexa_dblp_goldstandardIdDuplicates",
            "imdbgoldFinalIdDuplicates",
            "bbc-music_groundTruthUTF8IdDuplicates"
        };

        for (int datasetIndex = 0; datasetIndex < d1Datasets.length; datasetIndex++) {
            Preprocessing preprocessing = new Preprocessing(datasetsPaths[datasetIndex] + d1Datasets[datasetIndex],
                    datasetsPaths[datasetIndex] + d2Datasets[datasetIndex]);

            List<AbstractBlock> valueBlocks = preprocessing.getPurgedBlocks(new StandardBlocking());

            IGroundTruthReader gtReader = new GtSerializationReader(datasetsPaths[datasetIndex] + duplicates[datasetIndex]);
            Set<IdDuplicates> duplicatePairs = gtReader.getDuplicatePairs(null);
            System.out.println("Pairs of duplicates\t:\t" + duplicatePairs.size());

            BlocksPerformance blpe = new BlocksPerformance(valueBlocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();

            long time1 = System.currentTimeMillis();
//            List<AbstractBlock> copyOfBlocks = new ArrayList<>(valueBlocks);
//            CardinalityNodePruning cnp = new CardinalityNodePruning(WeightingScheme.ARCS);
//            copyOfBlocks = cnp.refineBlocks(copyOfBlocks);
//            
//            blpe = new BlocksPerformance(copyOfBlocks, new BilateralDuplicatePropagation(duplicatePairs));
//            blpe.setStatistics();
//            blpe.printStatistics();

            IBlockProcessing blockCleaningMethod = new BlockFiltering(0.8);
            valueBlocks = blockCleaningMethod.refineBlocks(valueBlocks);

            RandomParallelization rp = new RandomParallelization(8, WeightingScheme.CBS);
            final List<AbstractBlock> newBlocks = rp.refineBlocks(valueBlocks);

            long time2 = System.currentTimeMillis();
            System.out.println("Overhead time\t:\t" + (time2-time1));
            
            EntitySerializationReader esr = new EntitySerializationReader(mainDirectory);
            esr.storeSerializedObject(newBlocks, datasetsPaths + "blocks");
            
            blpe = new BlocksPerformance(newBlocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();
        }
    }
}
