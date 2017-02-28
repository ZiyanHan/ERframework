package NewApproaches;

import BlockProcessing.ComparisonRefinement.NewCardinalityNodePruning;
import BlockProcessing.ComparisonRefinement.NewNeighborCardinalityNodePruning;
import DataModel.AbstractBlock;
import DataModel.Comparison;
import DataModel.DecomposedBlock;
import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataModel.SimilarityPairs;
import DataReader.AbstractReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import RankAggregation.AbstractRankAggregation;
import RankAggregation.BordaCount;
import Utilities.BlocksPerformance;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author vefthym
 */
public class LocalRankAggregation {

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
        int datasetLimit = preprocessing.getDatasetLimit();

        IGroundTruthReader gtReader = new GtSerializationReader(gtPath);
        final BilateralDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme valueWScheme : WeightingScheme.values()) {
            System.out.println("\n\nMeta-blocking value weighting scheme: "+valueWScheme);
            List<AbstractBlock> copyOfVBlocks1 = new ArrayList<>(valueBlocks);
            NewCardinalityNodePruning cnpVB = new NewCardinalityNodePruning(valueWScheme);
            cnpVB.refineBlocks(copyOfVBlocks1);
            Comparison[][] valueComparisons = cnpVB.getNearestEntities();
            
//            for (WeightingScheme neighborWScheme : WeightingScheme.values()) {
            WeightingScheme neighborWScheme = valueWScheme; //TODO: comment out when the line above is not in comments
                System.out.println("\nMeta-blocking neighbor weighting scheme: "+neighborWScheme);
                
                //Option 1: Co-occuring neighbors
                List<AbstractBlock> copyOfVBlocks2 = new ArrayList<>(valueBlocks);
                NewNeighborCardinalityNodePruning ncnpVB = new NewNeighborCardinalityNodePruning(neighborIds, neighborWScheme);
                ncnpVB.refineBlocks(copyOfVBlocks2);
                Comparison[][] neighborComparisons = ncnpVB.getNearestEntities();
                
                
                //Option 2: Neighbor Blocking
               /* final List<AbstractBlock> neighborBlocks = preprocessing.getNeighborBlockingBlocks();                
                NewCardinalityNodePruning cnpNB = new NewCardinalityNodePruning(valueWScheme);
                cnpNB.refineBlocks(neighborBlocks);
                Comparison[][] neighborComparisons = cnpNB.getNearestEntities();*/

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
                Set<Integer> acceptableIds1 = preprocessing.getAcceptableIds1();
                Set<Integer> acceptableIds2 = preprocessing.getAcceptableIds2();                                
                for (int i = 0; i < maxEntities; ++i) { //for each entity i 
                    if (!acceptableIds1.contains(i) && !acceptableIds2.contains(i-datasetLimit)) {
                        continue;
                    }
//                    valueComparisons[i] = null; //REMOVE!!!! only for testing reasons to check value-/neighbor-only reciprocity
                    AbstractRankAggregation ra = new BordaCount(valueComparisons[i], neighborComparisons[i]);
//                    AbstractRankAggregation ra = new BordaCount(neighborComparisons[i], valueComparisons[i]);
                    //System.out.println("Adding the top aggr. comparison for entity "+i);
                    SimilarityPairs aggr = ra.getAggregation();
                    if (aggr.getNoOfComparisons() > 0) {                            
                        topComparisons[i] = aggr.getPairIterator().next(); //add the first (top) aggr. comparison for entity i
    //                    
    //                    if (neighborComparisons[i] != null && neighborComparisons[i].length > 0) {
    //                        if (!valueComparisons[i][0].equals(neighborComparisons[i][0])) {
    //                            System.out.println("The top value comparison for entity "+i+" is "+valueComparisons[i][0].getEntityId1()+", "+valueComparisons[i][0].getEntityId2());
    //                            System.out.println("The top neihgbor comparison for entity "+i+" is "+neighborComparisons[i][0].getEntityId1()+", "+neighborComparisons[i][0].getEntityId2());
    //                        }
    //                    } else {
    //                        System.out.println("No comparison for this entity from neighbors.");
    //                    }
    //                    System.out.println("The top comparison for entity "+i+" is "+topComparisons[i].getEntityId1()+", "+topComparisons[i].getEntityId2());
    //                    System.out.println("The top comparisons for entity "+i+" are:"); 
    //                    PairIterator pairs = aggr.getPairIterator();
    //                    while (pairs.hasNext()) {
    //                        Comparison next = pairs.next();
    //                        System.out.println(next.getEntityId1()+","+next.getEntityId2()+":"+next.getUtilityMeasure());
    //                    }
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
                    int j = curr.getEntityId2() + datasetLimit;
                    if (curr.equals(topComparisons[j])) { //then this is a reciprocal comparison                        
                        finalComparisons.add(curr);
                        topComparisons[j] = null; //do not check this entity again
                    }
                }
                addDecomposedBlock(finalComparisons, finalMatchesAsBlocks);

                BlocksPerformance bPer = new BlocksPerformance(finalMatchesAsBlocks, duplicatePropagation);
                bPer.setStatistics();
                bPer.printStatistics();
                //debugging follows:
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
                
//            } //end of for all meta-blocking weighting schemes for neighbors
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
