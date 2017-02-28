package NewApproaches;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import DataModel.AbstractBlock;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import EntityClustering.IEntityClustering;
import EntityClustering.UniqueMappingClustering;
import RankAggregation.AbstractRankAggregation;
import RankAggregation.BordaCount;
import Utilities.ClustersPerformance;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class Slide8 {

    public static void main(String[] args) {
        Set<String> acceptableTypes = new HashSet<>();
//        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";
        String mainDirectory = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";

//        String entitiesPath1 = mainDirectory + "rexaProfiles";
//        String entitiesPath2 = mainDirectory + "swetodblp_april_2008Profiles";
//        String gtPath = mainDirectory + "rexa_dblp_goldstandardIdDuplicates";
        String entitiesPath1 = mainDirectory + "restaurant1Profiles";
        String entitiesPath2 = mainDirectory + "restaurant2Profiles";
        String gtPath = mainDirectory + "restaurantIdDuplicates";
        
        if (args.length == 3) {
            entitiesPath1 = args[0];
            entitiesPath2 = args[1];
            gtPath = args[2];
        }

        Preprocessing valueBlocking = new Preprocessing(entitiesPath1, entitiesPath2);
        final List<AbstractBlock> valueBlocks = valueBlocking.getTokenBlockingBlocks();

        String neighborProfilesPath1 = entitiesPath1.replaceAll("Profiles$", "NeighborProfiles");
        String neighborProfilesPath2 = entitiesPath2.replaceAll("Profiles$", "NeighborProfiles");
        
        //if input does not exist, run the necessary jobs to create it
        File npp1 = new File(neighborProfilesPath1);
        if (!npp1.exists()) {
            GetNeighbors.main(Arrays.copyOf(args, 2));
            GetNeighborProfiles.main(Arrays.copyOf(args, 2));
        }
        
        Preprocessing neighborBlocking = new Preprocessing(neighborProfilesPath1, neighborProfilesPath2);
        final List<AbstractBlock> neighborBlocks = neighborBlocking.getTokenBlockingBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(gtPath);
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            System.out.println(wScheme);
            List<AbstractBlock> copyOfVBlocks = new ArrayList<>(valueBlocks);
            CardinalityNodePruning cnpVB = new CardinalityNodePruning(wScheme);
            copyOfVBlocks = cnpVB.refineBlocks(copyOfVBlocks);
            
            List<AbstractBlock> copyOfNBlocks = new ArrayList<>(neighborBlocks);
            CardinalityNodePruning cnpNB = new CardinalityNodePruning(wScheme);
            copyOfNBlocks = cnpNB.refineBlocks(copyOfNBlocks);

            // rank aggregation
            AbstractRankAggregation ra = new BordaCount(copyOfVBlocks, copyOfNBlocks);
            SimilarityPairs aggregation = ra.getAggregation(); 
            
            // clustering
            System.out.println("Running clustering...");
            IEntityClustering clustering = new UniqueMappingClustering();
//            clustering.setSimilarityThreshold(1.4 * Math.max(ra.getInputQueue1().length, ra.getInputQueue2().length)); //rule of thumb
            clustering.setSimilarityThreshold(00); //rule of thumb
            List<EquivalenceCluster> entityClusters = clustering.getDuplicates(aggregation);
            
            ClustersPerformance performance = new ClustersPerformance(entityClusters, duplicatePropagation);
            performance.setStatistics();
            performance.printStatistics();
        }
    }
}
