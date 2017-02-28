package NewApproaches;

import BlockProcessing.ComparisonRefinement.NeighborCardinalityNodePruning;
import DataModel.AbstractBlock;
import DataReader.AbstractReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.BlocksPerformance;
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
public class GetNeighborCnpPerformance {

    public static void main(String[] args) {
        Set<String> acceptableTypes = new HashSet<>();
        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";

        String entitiesPath1 = mainDirectory + "rexaProfiles";
        String entitiesPath2 = mainDirectory + "swetodblp_april_2008Profiles";
        String gtPath = mainDirectory + "rexa_dblp_goldstandardIdDuplicates";
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
            MergeNeighbors.main(Arrays.copyOf(args, 2));
        }
        
        int[][] neighborIds = (int[][]) AbstractReader.loadSerializedObject(neighborIdsPath);
        
        Preprocessing valueBlocking = new Preprocessing(entitiesPath1, entitiesPath2);
        final List<AbstractBlock> valueBlocks = valueBlocking.getTokenBlockingBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(gtPath);
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            List<AbstractBlock> copyOfVBlocks2 = new ArrayList<>(valueBlocks);
            NeighborCardinalityNodePruning ncnpVB = new NeighborCardinalityNodePruning(neighborIds, wScheme);
            copyOfVBlocks2 = ncnpVB.refineBlocks(copyOfVBlocks2);
            
            BlocksPerformance blpe = new BlocksPerformance(copyOfVBlocks2, duplicatePropagation);
            blpe.setStatistics();
            blpe.printStatistics();
            System.out.println(wScheme);
        }
    }
}
