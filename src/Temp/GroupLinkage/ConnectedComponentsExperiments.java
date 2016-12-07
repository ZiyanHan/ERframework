package Temp.GroupLinkage;

import BlockBuilding.IBlockBuilding;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.EntityReader.IEntityReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import EntityClustering.ConnectedComponentsClustering;
import EntityClustering.IEntityClustering;
import EntityMatching.GroupLinkage;
import Utilities.BlocksPerformance;
import Utilities.ClustersPerformance;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.UnilateralDuplicatePropagation;
import Utilities.Enumerations.BlockBuildingMethod;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class ConnectedComponentsExperiments {

    public static void main(String[] args) {
        BlockBuildingMethod blockingWorkflow = BlockBuildingMethod.STANDARD_BLOCKING;

        String[] datasetProfiles = {
            "/home/gpapadakis/data/csvProfiles/restaurantProfiles",
            "/home/gpapadakis/data/csvProfiles/coraProfiles",
            "/home/gpapadakis/data/csvProfiles/cddbProfiles",
            "/home/gpapadakis/data/csvProfiles/abtBuyProfiles",
            "/home/gpapadakis/data/csvProfiles/amazonGpProfiles",
            "/home/gpapadakis/data/csvProfiles/dblpAcmProfiles",
            "/home/gpapadakis/data/csvProfiles/dblpScholarProfiles",
            "/home/gpapadakis/data/csvProfiles/moviesProfiles"
        };

        String[] datasetGroundtruth = {
            "/home/gpapadakis/data/csvProfiles/restaurantIdDuplicates",
            "/home/gpapadakis/data/csvProfiles/coraIdDuplicates",
            "/home/gpapadakis/data/csvProfiles/cddbIdDuplicates",
            "/home/gpapadakis/data/csvProfiles/abtBuyIdDuplicates",
            "/home/gpapadakis/data/csvProfiles/amazonGpIdDuplicates",
            "/home/gpapadakis/data/csvProfiles/dblpAcmIdDuplicates",
            "/home/gpapadakis/data/csvProfiles/dblpScholarIdDuplicates",
            "/home/gpapadakis/data/csvProfiles/moviesIdDuplicates"
        };

        for (int datasetId = 0; datasetId < datasetProfiles.length; datasetId++) {
            System.out.println("\n\n\n\n\nCurrent dataset id\t:\t" + datasetId);;

            IEntityReader eReader = new EntitySerializationReader(datasetProfiles[datasetId]);
            List<EntityProfile> profiles = eReader.getEntityProfiles();
            System.out.println("Input Entity Profiles\t:\t" + profiles.size());

            IGroundTruthReader gtReader = new GtSerializationReader(datasetGroundtruth[datasetId]);
            final AbstractDuplicatePropagation duplicatePropagation = new UnilateralDuplicatePropagation(gtReader.getDuplicatePairs(eReader.getEntityProfiles()));
            System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

            IBlockBuilding blockBuildingMethod = BlockBuildingMethod.getDefaultConfiguration(blockingWorkflow);
            List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profiles, null);
            System.out.println("Original blocks\t:\t" + blocks.size());

            IBlockProcessing blockCleaningMethod = BlockBuildingMethod.getDefaultBlockCleaning(blockingWorkflow);
            if (blockCleaningMethod != null) {
                blocks = blockCleaningMethod.refineBlocks(blocks);
            }

            IBlockProcessing comparisonCleaningMethod = BlockBuildingMethod.getDefaultComparisonCleaning(blockingWorkflow);
            if (comparisonCleaningMethod != null) {
                blocks = comparisonCleaningMethod.refineBlocks(blocks);
            }

            BlocksPerformance blp = new BlocksPerformance(blocks, duplicatePropagation);
            blp.setStatistics();
            blp.printStatistics();

            double bestFMeasure = Double.MIN_VALUE;
            double bestGlThreshold = Double.MIN_VALUE;
            double bestThreshold = Double.MIN_VALUE;
            RepresentationModel bestRepModel = null;
            SimilarityMetric bestSimMetric = null;
            for (RepresentationModel repModel : RepresentationModel.values()) {
                System.out.println("\n\nCurrent model\t:\t" + repModel.toString());

                List<SimilarityMetric> simMetrics = SimilarityMetric.getModelCompatibleSimMetrics(repModel);
                for (SimilarityMetric simMetric : simMetrics) {
                    System.out.println("Current similarity metric\t:\t" + simMetric);

                    for (double glTh = 0.1; glTh < 1.0; glTh += 0.1) {
                        final List<AbstractBlock> copyOfBlocks = new ArrayList<>(blocks);

                        GroupLinkage gl = new GroupLinkage(repModel, simMetric);
                        gl.setSimilarityThreshold(glTh);
                        SimilarityPairs simPairs = gl.executeComparisons(copyOfBlocks, profiles);

                        for (double th = 0.1; th < 1.0; th += 0.1) {
                            IEntityClustering ec = new ConnectedComponentsClustering();
                            ec.setSimilarityThreshold(th);

                            List<EquivalenceCluster> entityClusters = ec.getDuplicates(simPairs);

                            ClustersPerformance clp = new ClustersPerformance(entityClusters, duplicatePropagation);
                            clp.setStatistics();
                            clp.printStatistics();

                            double currentFMeasure = clp.getFMeasure();
                            if (bestFMeasure < currentFMeasure) {
                                bestFMeasure = currentFMeasure;
                                bestGlThreshold = glTh;
                                bestRepModel = repModel;
                                bestSimMetric = simMetric;
                                bestThreshold = th;
                            }
                        }
                    }
                }
            }

            System.out.println("\n\n\n\n\nBest configuration...");
            System.out.println("Best F-Measure\t:\t" + bestFMeasure);
            System.out.println("Best Group Linkage Threshold\t:\t" + bestGlThreshold);
            System.out.println("Best Representation Model\t:\t" + bestRepModel);
            System.out.println("Best Similarity Metric\t:\t" + bestSimMetric);
            System.out.println("Best Threshold\t:\t" + bestThreshold);
        }
    }
}
