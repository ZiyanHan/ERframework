package NewApproaches;

import BlockProcessing.ComparisonRefinement.NewCardinalityNodePruning;
import BlockProcessing.ComparisonRefinement.NewNeighborCardinalityNodePruning;
import DataModel.AbstractBlock;
import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.AbstractReader;
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
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author G.A.P. II
 */
public class AlternativeSlide8New {

    public static void main(String[] args) {
        //Restaurant
        String mainDirectory = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        String entitiesPath1 = mainDirectory + "restaurant1Profiles";
        String entitiesPath2 = mainDirectory + "restaurant2Profiles";
        String gtPath = mainDirectory + "restaurantIdDuplicates";        
        
        //Rexa-DBLP
//        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";
//        String mainDirectory = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\rexa-dblp\\";
//        String entitiesPath1 = mainDirectory + "rexaProfiles";
//        String entitiesPath2 = mainDirectory + "swetodblp_april_2008Profiles";
//        String gtPath = mainDirectory + "rexa_dblp_goldstandardIdDuplicates";
               
        
        
        String neighborIdsPath = mainDirectory + "totalNeighborIds";
        
        if (args.length == 3) {
            entitiesPath1 = args[0];
            entitiesPath2 = args[1];
            gtPath = args[2];
            neighborIdsPath = new File(args[0]).getParent()+File.separator+"totalNeighborIds";
        }
        
        //if input does not exist, run the necessary tasks to create it
        File nip = new File(neighborIdsPath);
        if (!nip.exists()) {
            if (args.length == 0) {
                args = new String[2];
                args[0] = entitiesPath1;
                args[1] = entitiesPath2;
            }
            MergeNeighbors.main(Arrays.copyOf(args, 2));
        }
        
        int[][] neighborIds = (int[][]) AbstractReader.loadSerializedObject(neighborIdsPath);
        
        Preprocessing valueBlocking = new Preprocessing(entitiesPath1, entitiesPath2);
        final List<AbstractBlock> valueBlocks = valueBlocking.getBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(gtPath);
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            List<AbstractBlock> copyOfVBlocks1 = new ArrayList<>(valueBlocks);
            NewCardinalityNodePruning cnpVB = new NewCardinalityNodePruning(wScheme);
            cnpVB.refineBlocks(copyOfVBlocks1);
            Comparison[][] valueComparisons = cnpVB.getNearestEntities();
            
            List<AbstractBlock> copyOfVBlocks2 = new ArrayList<>(valueBlocks);
            NewNeighborCardinalityNodePruning ncnpVB = new NewNeighborCardinalityNodePruning(neighborIds, wScheme);
            ncnpVB.refineBlocks(copyOfVBlocks2);
            Comparison[][] neighborComparisons = ncnpVB.getNearestEntities();
            
            
            // rank aggregation
            int valueCompCounter = 0;
            for (Comparison[] comparisonArray : valueComparisons) {
                if (comparisonArray != null) {
                    valueCompCounter += comparisonArray.length;
                }
            }
            
            int neighborCompCounter = 0;
            for (Comparison[] comparisonArray : neighborComparisons) {
                if (comparisonArray != null) {
                    neighborCompCounter += comparisonArray.length;
                }
            }     
            int compCounter = Math.max(valueCompCounter, neighborCompCounter);  
            int maxEntities = Math.max(valueComparisons.length, neighborComparisons.length);
            
            SimilarityPairs aggregation = new SimilarityPairs(true, compCounter);
            TreeSet<Comparison> pq = new TreeSet<>();
            System.out.println("Found "+compCounter+" total comparisons");
            for (int i = 0; i < maxEntities; ++i) {                  
                AbstractRankAggregation ra = new BordaCount(valueComparisons[i], neighborComparisons[i]);                
                System.out.println("Adding comparisons to the final aggregation for entity "+i);
                SimilarityPairs aggr = ra.getAggregation();
                if (aggr.getNoOfComparisons() > 0) {
                    aggregation.addComparisons(ra.getAggregation());
                }
            }            
            
            // clustering
            System.out.println("Running clustering...");
            IEntityClustering clustering = new UniqueMappingClustering();
            clustering.setSimilarityThreshold(1.4 * aggregation.getNoOfComparisons()); //rule of thumb
            List<EquivalenceCluster> entityClusters = clustering.getDuplicates(aggregation);
            
            ClustersPerformance performance = new ClustersPerformance(entityClusters, duplicatePropagation);
            performance.setStatistics();
            performance.printStatistics();
        }
    }
}
