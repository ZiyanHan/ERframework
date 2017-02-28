package NewApproaches;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import DataModel.AbstractBlock;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.BlocksPerformance;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class GetValueBlockingPerformance {

    public static void main(String[] args) {
        Set<String> acceptableTypes = new HashSet<>();
        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";

        String neighborProfilesPath1 = mainDirectory + "rexaNeighborProfile";
        String neighborProfilesPath2 = mainDirectory + "swetodblp_april_2008NeighborProfile";
        String gtPath = mainDirectory + "rexa_dblp_goldstandardIdDuplicates";
        
        if (args.length == 3) {
            neighborProfilesPath1 = args[0];
            neighborProfilesPath2 = args[1];
            gtPath = args[2];
        }

        Preprocessing neighborBlocking = new Preprocessing(neighborProfilesPath1, neighborProfilesPath2);
        final List<AbstractBlock> neighborBlocks = neighborBlocking.getTokenBlockingBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(gtPath);
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        BlocksPerformance blpe = new BlocksPerformance(neighborBlocks, duplicatePropagation);
        blpe.setStatistics();
        blpe.printStatistics();

        for (WeightingScheme wScheme : WeightingScheme.values()) {            
            List<AbstractBlock> copyOfBlocks = new ArrayList<>(neighborBlocks);

            CardinalityNodePruning cnp = new CardinalityNodePruning(wScheme);
            copyOfBlocks = cnp.refineBlocks(copyOfBlocks);

            BlocksPerformance blp = new BlocksPerformance(copyOfBlocks, duplicatePropagation);
            blp.setStatistics();
            blp.printStatistics();
            System.out.println(wScheme);
        }
    }
}
