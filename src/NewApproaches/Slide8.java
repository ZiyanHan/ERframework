package NewApproaches;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import DataModel.AbstractBlock;
import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import EntityClustering.IEntityClustering;
import EntityClustering.UniqueMappingClustering;
import RankAggregation.AbstractRankAggregation;
import RankAggregation.BordaCount;
import Utilities.ClustersPerformance;
import Utilities.Comparators.ComparisonWeightComparator;
import Utilities.Comparators.ReverseComparisonWeightComparator;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author G.A.P. II
 */
public class Slide8 {

    public static void main(String[] args) {
//        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";
        String mainDirectory = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";

//        String entitiesPath1 = mainDirectory + "rexaProfiles";
//        String entitiesPath2 = mainDirectory + "swetodblp_april_2008Profiles";
        String entitiesPath1 = mainDirectory + "restaurant1Profiles";
        String entitiesPath2 = mainDirectory + "restaurant2Profiles";

        Preprocessing valueBlocking = new Preprocessing(entitiesPath1, entitiesPath2);
        final List<AbstractBlock> valueBlocks = valueBlocking.getBlocks();

        String neighborProfilesPath1 = entitiesPath1.replaceAll("Profiles$", "NeighborProfiles");
        String neighborProfilesPath2 = entitiesPath2.replaceAll("Profiles$", "NeighborProfiles");

        Preprocessing neighborBlocking = new Preprocessing(neighborProfilesPath1, neighborProfilesPath2);
        final List<AbstractBlock> neighborBlocks = neighborBlocking.getBlocks();

//        IGroundTruthReader gtReader = new GtSerializationReader(mainDirectory + "rexa_dblp_goldstandardIdDuplicates");
        IGroundTruthReader gtReader = new GtSerializationReader(mainDirectory + "restaurantIdDuplicates");
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            List<AbstractBlock> copyOfVBlocks = new ArrayList<>(valueBlocks);
            CardinalityNodePruning cnpVB = new CardinalityNodePruning(wScheme);
            copyOfVBlocks = cnpVB.refineBlocks(copyOfVBlocks);
            
            List<AbstractBlock> copyOfNBlocks = new ArrayList<>(neighborBlocks);
            CardinalityNodePruning cnpNB = new CardinalityNodePruning(wScheme);
            copyOfNBlocks = cnpNB.refineBlocks(copyOfNBlocks);

            // rank aggregation
            System.out.println("Running rank aggregation...");
            Queue<Comparison> valueQ = new PriorityQueue<>(new ReverseComparisonWeightComparator());           
            for (AbstractBlock block : copyOfVBlocks) {
                final Iterator<Comparison> iterator = block.getComparisonIterator();           
                while (iterator.hasNext()) {
                    Comparison currentComparison = iterator.next();                    
                    valueQ.add(currentComparison);                    
                }
            }
            Comparison[] valuesArray = new Comparison[valueQ.size()];
            valuesArray = valueQ.toArray(valuesArray);
            
            Queue<Comparison> neighborQ = new PriorityQueue<>(new ReverseComparisonWeightComparator());           
            for (AbstractBlock block : copyOfNBlocks) {
                final Iterator<Comparison> iterator = block.getComparisonIterator();           
                while (iterator.hasNext()) {
                    Comparison currentComparison = iterator.next();                    
                    neighborQ.add(currentComparison);                    
                }
            }            
            Comparison[] neighborsArray = new Comparison[neighborQ.size()];
            neighborsArray = neighborQ.toArray(neighborsArray);
            
            AbstractRankAggregation ra = new BordaCount(valuesArray, neighborsArray);
            SimilarityPairs aggregation = ra.getAggregation(); 
            
            // clustering
            System.out.println("Running clustering...");
            IEntityClustering clustering = new UniqueMappingClustering();
            List<EquivalenceCluster> entityClusters = clustering.getDuplicates(aggregation);
            
            ClustersPerformance performance = new ClustersPerformance(entityClusters, duplicatePropagation);
            performance.setStatistics();
            performance.printStatistics();
            //return; //only for debugging
        }
    }
}
