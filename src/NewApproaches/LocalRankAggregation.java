package NewApproaches;

import BlockProcessing.ComparisonRefinement.NewCardinalityNodePruning;
import BlockProcessing.ComparisonRefinement.NewNeighborCardinalityNodePruning;
import DataModel.AbstractBlock;
import DataModel.Comparison;
import DataModel.DecomposedBlock;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.AbstractReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import EntityClustering.IEntityClustering;
import EntityClustering.UniqueMappingClustering;
import RankAggregation.AbstractRankAggregation;
import RankAggregation.BordaCount;
import Utilities.BlocksPerformance;
import Utilities.ClustersPerformance;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author G.A.P. II
 */
public class LocalRankAggregation {

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
        int datasetLimit = valueBlocking.getDatasetLimit();

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
            int valueComparisonsCounter = 0;
            for (Comparison[] comparisonArray : valueComparisons) {
                if (comparisonArray != null) {
                    valueComparisonsCounter += comparisonArray.length;
                }
            }            
            int neighborComparisonsCounter = 0;
            for (Comparison[] comparisonArray : neighborComparisons) {
                if (comparisonArray != null) {
                    neighborComparisonsCounter += comparisonArray.length;
                }
            }     
            int comparisonsCounter = Math.max(valueComparisonsCounter, neighborComparisonsCounter);  
            int maxEntities = Math.max(valueComparisons.length, neighborComparisons.length);
            
            Comparison[] topComparisons = new Comparison[maxEntities]; //keeps the top comparison for each entity
            System.out.println("Found "+comparisonsCounter+" total comparisons");
            for (int i = 0; i < maxEntities; ++i) { //for each entity i                
                AbstractRankAggregation ra = new BordaCount(valueComparisons[i], neighborComparisons[i]);                
               // System.out.println("Adding the top aggr. comparison for entity "+i);
                SimilarityPairs aggr = ra.getAggregation();
                if (aggr.getNoOfComparisons() > 0) {              
                    SimilarityPairs localPairs = ra.getAggregation();
                    if (localPairs.getNoOfComparisons() > 0) {        
                        topComparisons[i] = localPairs.getPairIterator().next(); //add the first (top) aggr. comparison for entity i
//                        System.out.println("The top comparison for entity "+i+" is "+topComparisons[i].getEntityId1()+", "+topComparisons[i].getEntityId2());
                    }                    
                }
            }
            
            //get the reciprocal comparisons only
            List<Comparison> finalComparisons = new ArrayList<>();
            List<AbstractBlock> finalMatchesAsBlocks = new ArrayList<>();
            for (int i = 0; i < datasetLimit; ++i) {
                Comparison curr = topComparisons[i];
                if (curr == null)  {
                    continue;
                }
//                int j = (curr.getEntityId1() == i) ? curr.getEntityId2() : curr.getEntityId1();
                int j = curr.getEntityId2() + datasetLimit;
//                System.out.println("Checking if the comparison:("+curr.getEntityId1()+","+curr.getEntityId2()+") is the same for entity "+j);
//                System.out.println("...the top copmarison of entity "+j+" is ("+topComparisons[j].getEntityId1()+", "+topComparisons[j].getEntityId2()+")");
                if (curr.equals(topComparisons[j])) {
//                    System.out.println("Found a reciprocal comparison!");
                    finalComparisons.add(curr);
                    topComparisons[j] = null; //do not check this again
                }
            }
            addDecomposedBlock(finalComparisons, finalMatchesAsBlocks);
            
            BlocksPerformance bPer = new BlocksPerformance(finalMatchesAsBlocks, duplicatePropagation);
            bPer.setStatistics();
            bPer.printStatistics();
            
        }
    }
            
    protected static void addDecomposedBlock(Collection<Comparison> comparisons, List<AbstractBlock> newBlocks) {
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

        newBlocks.add(new DecomposedBlock(true, similarities, entityIds1, entityIds2));
    }
}
