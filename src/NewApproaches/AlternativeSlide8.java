package NewApproaches;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import BlockProcessing.ComparisonRefinement.NeighborCardinalityNodePruning;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataModel.EquivalenceCluster;
import DataModel.IdDuplicates;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class AlternativeSlide8 {

    public static void main(String[] args) {
        Set<String> acceptableTypes = new HashSet<>();
        
        //Restaurant
        String mainDirectory = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        String entitiesPath1 = mainDirectory + "restaurant1Profiles";
        String entitiesPath2 = mainDirectory + "restaurant2Profiles";
        String gtPath = mainDirectory + "restaurantIdDuplicates";        
        acceptableTypes.add("http://www.okkam.org/ontology_restaurant2.owl#Restaurant");
        acceptableTypes.add("http://www.okkam.org/ontology_restaurant1.owl#Restaurant");
        
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
        
        Preprocessing preprocessing = new Preprocessing(entitiesPath1, entitiesPath2, acceptableTypes);
        final List<AbstractBlock> valueBlocks = preprocessing.getTokenBlockingBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(gtPath);
        final BilateralDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            System.out.println("\n\n"+wScheme);
            List<AbstractBlock> copyOfVBlocks1 = new ArrayList<>(valueBlocks);
            CardinalityNodePruning cnpVB = new CardinalityNodePruning(wScheme);
            copyOfVBlocks1 = cnpVB.refineBlocks(copyOfVBlocks1);
            
            List<AbstractBlock> copyOfVBlocks2 = new ArrayList<>(valueBlocks);
            NeighborCardinalityNodePruning ncnpVB = new NeighborCardinalityNodePruning(neighborIds, wScheme);
            copyOfVBlocks2 = ncnpVB.refineBlocks(copyOfVBlocks2);
            
            // rank aggregation
            Set<Integer> acceptableIds1 = preprocessing.getAcceptableIds1();
            Set<Integer> acceptableIds2 = preprocessing.getAcceptableIds2();
            System.out.println(Arrays.toString(acceptableIds1.toArray()));
            System.out.println(Arrays.toString(acceptableIds2.toArray()));
            AbstractRankAggregation ra = new BordaCount(copyOfVBlocks1, copyOfVBlocks2, acceptableIds1, acceptableIds2);
            SimilarityPairs aggregation = ra.getAggregation(); 
            
            // clustering
            System.out.println("Running clustering...");
            IEntityClustering clustering = new UniqueMappingClustering();
//            clustering.setSimilarityThreshold(1.4 * Math.max(ra.getInputQueue1().length, ra.getInputQueue2().length)); //rule of thumb
            clustering.setSimilarityThreshold(170);
            List<EquivalenceCluster> entityClusters = clustering.getDuplicates(aggregation);
            
            ClustersPerformance performance = new ClustersPerformance(entityClusters, duplicatePropagation);
//            performance.setStatistics(preprocessing.getAcceptableIds1(), preprocessing.getAcceptableIds2());
            performance.setStatistics();
            performance.printStatistics();
            //debugging
            /*
            Set<IdDuplicates> fps = duplicatePropagation.getFalseMatches();
                List<EntityProfile> profiles1 = preprocessing.getProfiles1();
                List<EntityProfile> profiles2 = preprocessing.getProfiles2();
                for (IdDuplicates fp : fps) {                    
                    System.out.println(fp);
                    int entityId1 = fp.getEntityId1();
                    int entityId2 = fp.getEntityId2();
                    System.out.println("Entity1: "+entityId1);
                    System.out.println(profiles1.get(entityId1));
                    System.out.println("Entity2: "+entityId2);
                    System.out.println(profiles2.get(entityId2));
                }
            */
        }
    }
}
