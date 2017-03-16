package NewApproaches.MulticoreProcessing;

import BlockBuilding.StandardBlocking;
import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import DataModel.AbstractBlock;
import DataModel.Attribute;
import DataModel.Comparison;
import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import EntityMatching.IEntityMatching;
import EntityMatching.ProfileMatcher;
import NewApproaches.Preprocessing;
import Utilities.BlocksPerformance;
import Utilities.Comparators.ComparisonWeightComparator;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import Utilities.Enumerations.WeightingScheme;
import Utilities.TextModels.AbstractModel;
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
public class BaselineParallelization extends CardinalityNodePruning {

    protected boolean isCleanCleanER;
    protected int entityCounter;
    protected int noOfNodes;

    protected AbstractModel[] entityModels;
    protected IEntityMatching matcher;
    protected Integer[] sortedEntities;
    protected RandomThread[] threads;
    protected RepresentationModel representationModel;
    protected Queue<Comparison>[] globallyNearestEntities;
    protected Set<Comparison> totalComparisons;
    protected SimilarityMetric simMetric;

    public BaselineParallelization(int nodes, List<EntityProfile> profilesD1, List<EntityProfile> profilesD2,
            RepresentationModel model, SimilarityMetric metric) {
        super(WeightingScheme.ARCS);
        noOfNodes = nodes;
        nodeCentric = true;
        representationModel = model;
        simMetric = metric;
        threads = new RandomThread[noOfNodes];
        
        buildModels(profilesD1, profilesD2);
    }

    @Override
    protected List<AbstractBlock> applyMainProcessing() {
        setLimits();
        setThreshold();
        sortEntities();

        entityCounter = 0;
        for (int i = 0; i < noOfNodes; i++) {
            threads[i] = new RandomThread();
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

    protected void buildModels(List<EntityProfile> profilesD1, List<EntityProfile> profilesD2) {
        int counter = 0;
        if (!cleanCleanER) {
            entityModels = new AbstractModel[profilesD1.size()];
            for (EntityProfile profile : profilesD1) {
                entityModels[counter] = RepresentationModel.getModel(representationModel, simMetric, profile.getEntityUrl());
                for (Attribute attribute : profile.getAttributes()) {
                    entityModels[counter].updateModel(attribute.getValue());
                }
                counter++;
            }
        } else {
            entityModels = new AbstractModel[profilesD1.size()+profilesD2.size()];
            for (EntityProfile profile : profilesD1) {
                entityModels[counter] = RepresentationModel.getModel(representationModel, simMetric, profile.getEntityUrl());
                for (Attribute attribute : profile.getAttributes()) {
                    entityModels[counter].updateModel(attribute.getValue());
                }
                counter++;
            }
            for (EntityProfile profile : profilesD2) {
                entityModels[counter] = RepresentationModel.getModel(representationModel, simMetric, profile.getEntityUrl());
                for (Attribute attribute : profile.getAttributes()) {
                    entityModels[counter].updateModel(attribute.getValue());
                }
                counter++;
            }
        }
    }

    protected synchronized int getNextEntityId() {
        if (entityCounter < sortedEntities.length) {
            return sortedEntities[entityCounter++];
        }
        return -1;
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

        protected double[] minimumWeights;

        protected Queue<Comparison>[] locallyNearestEntities;
        protected final Set<Integer> validEntities;

        public RandomThread() {
            minimumWeights = new double[noOfEntities];
            locallyNearestEntities = new PriorityQueue[noOfEntities];
            for (int i = 0; i < noOfEntities; i++) {
                locallyNearestEntities[i] = new PriorityQueue<Comparison>((int) (2 * threshold), new ComparisonWeightComparator());
            }

            validEntities = new HashSet<>();
        }

        protected Queue<Comparison>[] getLocallyNearestEntities() {
            return locallyNearestEntities;
        }

        protected double getWeight(int entityId, int neighborId) {
            return entityModels[entityId].getSimilarity(entityModels[neighborId]);
        }

        protected void processEntity(int entityId) {
            validEntities.clear();
            final int[] associatedBlocks = entityIndex.getEntityBlocks(entityId, 0);
            for (int blockIndex : associatedBlocks) {
                List<Integer> currentNeighbors = setNormalizedNeighborEntities(blockIndex, entityId);
                for (int neighborId : currentNeighbors) {
                    validEntities.add(neighborId);
                }
            }
        }

        @Override
        public void run() {
            int currentId = -1;
            while (0 <= (currentId = getNextEntityId())) {
                processEntity(currentId);
                verifyValidEntities(currentId);
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

            List<AbstractBlock> valueBlocks = preprocessing.getBlocks(new StandardBlocking());

            IGroundTruthReader gtReader = new GtSerializationReader(datasetsPaths[datasetIndex] + duplicates[datasetIndex]);
            Set<IdDuplicates> duplicatePairs = gtReader.getDuplicatePairs(null);
            System.out.println("Pairs of duplicates\t:\t" + duplicatePairs.size());

            BlocksPerformance blpe = new BlocksPerformance(valueBlocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();

            for (RepresentationModel repModel : RepresentationModel.values()) {
                System.out.println("\n\nCurrent model\t:\t" + repModel.toString());

                List<SimilarityMetric> simMetrics = SimilarityMetric.getModelCompatibleSimMetrics(repModel);
                for (SimilarityMetric simMetric : simMetrics) {
                    System.out.println("Current similarity metric\t:\t" + simMetric);

                    List<AbstractBlock> copyOfBlocks = new ArrayList<>(valueBlocks);

                    BaselineParallelization rp = new BaselineParallelization(8,
                            preprocessing.getProfiles1(), preprocessing.getProfiles2(),
                            repModel, simMetric);
                    final List<AbstractBlock> newBlocks = rp.refineBlocks(copyOfBlocks);

                    EntitySerializationReader esr = new EntitySerializationReader(mainDirectory);
                    esr.storeSerializedObject(newBlocks, datasetsPaths + "entities" + repModel + "_" + simMetric);
                }
            }
        }
    }
}
